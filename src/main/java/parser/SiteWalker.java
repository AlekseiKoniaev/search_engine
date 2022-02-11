package parser;

import indexer.PageIndexer;
import model.Page;
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
    
    private final Page page;
    
    
    public SiteWalker(String URL) {
        if (rootPath == null) {
            PageIndexer.setFields(DBConnection.getFields());
            rootPath = URL.endsWith("/") ? URL.substring(0, URL.length() - 1) : URL;
            page = new Page("/");
        } else {
            page = new Page(URL);
        }
    }
    
    @Override
    protected String compute() {
        
        if (visitPage()) {
    
            PageIndexer indexer = new PageIndexer(page);
            indexer.index();
            
            PageParser parser = new PageParser(rootPath, page);
            
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
            
            return page.getPath();
        } else {
            return "";
        }
    }
    
    private boolean visitPage() {
        String path = page.getPath();
        synchronized (visitedPages) {
            if (visitedPages.contains(path)) {
                return false;
            } else {
                visitedPages.add(path);
            }
        }
        synchronized (page) {
            try {
                Connection.Response response = Jsoup.connect(rootPath + path)
                        .userAgent(USER_AGENT)
                        .referrer(REFERRER)
                        .execute();
                page.setCode(response.statusCode());
                page.setDocument(response.parse());
            } catch (HttpStatusException e) {
                page.setCode(e.getStatusCode());
            } catch (SocketTimeoutException e) {
                page.setCode(0);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            
            DBConnection.insertPage(page);
        }
        
        return page.getCode() == 200;
    }
    
}
