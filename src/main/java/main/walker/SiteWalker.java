package main.walker;

import main.indexer.PageIndexer;
import main.model.Page;
import main.model.Site;
import main.repository.HibernateConnection;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

public class SiteWalker extends RecursiveTask<Void> {
    
    private static final String USER_AGENT = "Mozilla/5.0 (Windows; U; " +
            "WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";
    private static final String REFERRER = "http://www.google.com";
    
    
    private final Site site;
    private final Page page;
    private final Set<String> visitedPages;
    
    
    public SiteWalker(String URL) {
        URL = URL.endsWith("/") ? URL.substring(0, URL.length() - 1) : URL;
        site = new Site(URL);
        page = new Page("/");
        visitedPages = Collections.synchronizedSet(new HashSet<>());
        PageIndexer.setFields(HibernateConnection.getFields());
    }
    
    private SiteWalker(Site site, String path, Set<String> visitedPages) {
        this.site = site;
        page = new Page(path);
        this.visitedPages = visitedPages;
    }
    
    @Override
    protected Void compute() {
        
        if (visitPage()) {
    
            PageIndexer indexer = new PageIndexer(page);
            indexer.index();
            
            PageParser parser = new PageParser(site.getUrl(), page);
            
            Set<String> parsedPages = parser.parseLink();
            parsedPages.removeIf(visitedPages::contains);
            
            List<SiteWalker> walkerList = new ArrayList<>();
            for (String path : parsedPages) {
                SiteWalker walker = new SiteWalker(site, path, visitedPages);
                walker.fork();
                walkerList.add(walker);
            }
            
            for (SiteWalker walker : walkerList) {
                walker.join();
            }
    
        }
        return null;
    }
    
    private boolean visitPage() {
        String path = page.getPath();
        synchronized (site) {
            if (visitedPages.contains(path)) {
                return false;
            } else {
                visitedPages.add(path);
            }
        }
        synchronized (page) {
            try {
                Connection.Response response = Jsoup.connect(site.getUrl() + path)
                        .userAgent(USER_AGENT)
                        .referrer(REFERRER)
                        .execute();
                page.setCode(response.statusCode());
                page.setContent(response.parse().toString());
            } catch (HttpStatusException e) {
                page.setCode(e.getStatusCode());
            } catch (SocketTimeoutException e) {
                page.setCode(0);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            
            HibernateConnection.insertPage(page);
        }
        
        return page.getCode() == 200;
    }
    
}
