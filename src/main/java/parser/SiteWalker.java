package parser;

import indexer.PageIndexer;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import repository.DBConnection;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

public class SiteWalker extends RecursiveTask<String> {
    
    private static final String USER_AGENT = "Mozilla/5.0 (Windows; U; " +
            "WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";
    private static final String REFERRER = "http://www.google.com";
    private static final Set<String> visitedPages = Collections.synchronizedSet(new HashSet<>());
    
    private static String rootPath;
    
    private final String path;
    private int code;
    private String content;
    private Document document;
    
    
    public SiteWalker(String URL) {
        if (rootPath == null) {
            PageIndexer.setFields(DBConnection.getFields());
            rootPath = URL.endsWith("/") ? URL.substring(0, URL.length() - 1) : URL;
            path = "/";
        } else {
            this.path = URL;
        }
    }
    
    @Override
    protected String compute() {
        
        if (visitPage()) {
    
            PageIndexer indexer = new PageIndexer(path, document);
            indexer.index();
            
            PageParser parser = new PageParser(rootPath, document);
            
            Set<String> parsedPages = parser.parseLink();
            parsedPages.removeIf(visitedPages::contains);
            
            List<SiteWalker> walkerList = new ArrayList<>();
            for (String path : parsedPages) {
                SiteWalker walker = new SiteWalker(path);
                walker.fork();
                walkerList.add(walker);
            }
            
            for (SiteWalker walker : walkerList) {
                walker.join();
            }
            
            return path;
        } else {
            return "";
        }
    }
    
    private boolean visitPage() {
        synchronized (visitedPages) {
            if (visitedPages.contains(path)) {
                return false;
            } else {
                visitedPages.add(path);
            }
        }
        synchronized (path) {
            try {
                Connection.Response response = Jsoup.connect(rootPath + path)
                        .userAgent(USER_AGENT)
                        .referrer(REFERRER)
                        .execute();
                code = response.statusCode();
                document = response.parse();
            } catch (HttpStatusException e) {
                code = e.getStatusCode();
            } catch (SocketTimeoutException e) {
                code = 0;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            content = Objects.requireNonNullElse(document, "").toString();
            DBConnection.insertPage(path, code, content);
        }
        
        return code == 200;
    }
    
}
