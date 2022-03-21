package main.walker;

import lombok.Getter;
import main.config.WebConfig;
import main.indexer.PageIndexer;
import main.model.Page;
import main.model.Site;
import main.model.enums.Status;
import main.service.IndexingService;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

@Component
@Scope("prototype")
public class SiteWalker extends RecursiveAction {
    
    private ApplicationContext applicationContext;
    private IndexingService service;
    private WebConfig config;
    
    @Getter
    private final Page page;
    @Getter
    private final Site site;
    private final Set<String> visitedPages;
    
    
    public SiteWalker(Site site) {
        this(new Page("/"), site);
        page.setSiteId(site.getId());
    }
    
    public SiteWalker(Page page, Site site) {
        this(page, site, Collections.synchronizedSet(new HashSet<>()));
    }
    
    private SiteWalker(Page page, Site site, Set<String> visitedPages) {
        this.page = page;
        this.site = site;
        this.visitedPages = visitedPages;
    }
    
    
    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @Autowired
    public void setService(IndexingService service) {
        this.service = service;
    }
    
    @Autowired
    public void setConfig(WebConfig config) {
        this.config = config;
    }
    
    @Override
    protected void compute() {
    
        if (Thread.currentThread().isInterrupted()) {
            return;
        }
        
        if (visitPage()) {
    
            index();
    
            List<SiteWalker> walkerList = new ArrayList<>();
            for (String path : getParsedPages()) {
                Page page = new Page(path);
                page.setSiteId(site.getId());
                SiteWalker walker = applicationContext.getBean(SiteWalker.class, page, site, visitedPages);
                walkerList.add(walker);
            }
            
            if (walkerList.isEmpty()) {
                return;
            }
            
            invokeAll(walkerList);
        }
    }
    
    public void indexOnePage() {
        visitPage();
        index();
    }
    
    private boolean visitPage() {
        
        Site site = this.site;
        String path = page.getPath();
        
        synchronized (this.site) {
            site = service.getSiteService().getSiteByUrl(site.getUrl());
            if (visitedPages.contains(path) || site.getStatus() != Status.INDEXING) {
                return false;
            } else {
                visitedPages.add(path);
                site.updateStatusTime();
                service.getSiteService().updateStatusTime(site);
            }
        }
        
        synchronized (page) {
            try {
                Connection.Response response = Jsoup.connect(site.getUrl() + path)
                        .userAgent(config.getUserAgent())
                        .referrer(config.getReferrer())
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
            
            service.getPageService().savePage(page);
        }
        
        return page.getCode() == 200;
    }
    
    private void index() {
        PageIndexer indexer = applicationContext.getBean(PageIndexer.class, page, site);
        indexer.index();
    }
    
    private Set<String> getParsedPages() {
        PageParser parser = new PageParser(page.getDocument(), site.getUrl());
        Set<String> parsedPages = parser.parseLink();
        parsedPages.removeIf(visitedPages::contains);
        return parsedPages;
    }
    
}
