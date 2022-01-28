import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveTask;

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
    
        if (recordToDB()) {
            
            PageParser parser = new PageParser(rootPath, page.getContent());
            Set<String> parsedPages = new HashSet<>();
            for (String path : parser.getParsedPaths()) {
                if ( ! rootWalker.visitedPages.containsKey(path)) {
                    parsedPages.add(path);
                }
            }
            
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
    
    private boolean recordToDB() {
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
        
        page.setCode(code);
        page.setContent(content);
        rootWalker.visitedPages.put(page.getPath(), Boolean.TRUE);
        DBConnection.insertPage(page);
        
        return true;
    }
}
