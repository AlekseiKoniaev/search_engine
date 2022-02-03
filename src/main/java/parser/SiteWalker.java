package parser;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import repository.DBConnection;
import repository.DBStructure.Page;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

public class SiteWalker extends RecursiveTask<String> {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows; U; " +
            "WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";
    private static final String REFERRER = "http://www.google.com";
    
    private final SiteWalker rootWalker;
    private final String rootPath;
    private final Page page;
    private Map<String, Object> visitedPages;
    
    public SiteWalker(String URL) {
        rootWalker = this;
        rootPath = URL.endsWith("/") ? URL.substring(0, URL.length() - 1) : URL;
        page = new Page("/");
        visitedPages = new ConcurrentHashMap<>();
    }
    
    private SiteWalker(String path, SiteWalker rootWalker) {
        this.rootWalker = rootWalker;
        this.rootPath = rootWalker.rootPath;
        page = new Page(path);
    }
    
    @Override
    protected String compute() {
    
        if (visitPage()) {
            
            Set<String> parsedPages = parse();
            parsedPages.removeIf(path -> rootWalker.visitedPages.containsKey(path));
            
            List<SiteWalker> walkerList = new ArrayList<>();
            for (String path : parsedPages) {
                SiteWalker walker = new SiteWalker(path, rootWalker);
                walker.fork();
                walkerList.add(walker);
            }
            
            for (SiteWalker walker : walkerList) {
                walker.join();
            }
            
            return page.getPath();
        } else {
            return "";
        }
    }
    
    private boolean visitPage() {
        Document content = null;
        int code;
        
        synchronized (rootWalker) {
            if (rootWalker.visitedPages.containsKey(page.getPath())) {
                return false;
            }
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    
            try {
                Connection.Response response = Jsoup.connect(rootPath + page.getPath())
                        .userAgent(USER_AGENT)
                        .referrer(REFERRER)
                        .execute();
                code = response.statusCode();
                content = response.parse();
            } catch (HttpStatusException e) {
                code = e.getStatusCode();
            } catch (SocketTimeoutException e) {
                code = 0;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        
        savePage(code, Objects.requireNonNullElse(content, "").toString());
        
        return code == 200;
    }
    
    private void savePage(int code, String content) {
        page.setCode(code);
        page.setContent(content);
        rootWalker.visitedPages.put(page.getPath(), Boolean.TRUE);
        DBConnection.insertPageHibernate(page);
    }
    
    private Set<String> parse() {
        final String ROOT_REGEX = "(" + rootPath + ")?";
        final String LINK_REGEX = "/[\\w/]+(\\.html|\\.php)?$";
        return Jsoup.parse(page.getContent()).select("a[href]")
                .stream()
                .map(e -> e.attr("href"))
                .filter(l -> (l.matches(ROOT_REGEX + LINK_REGEX)))
                .map(l -> l = l.replaceFirst(rootPath, ""))
                .collect(Collectors.toSet());
    }
}
