package main.walker;

import lombok.Getter;
import main.config.YAMLConfig;
import main.indexer.PageIndexer;
import main.model.Page;
import main.model.Site;
import main.model.enums.Status;
import main.service.impl.IndexingServiceImpl;
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
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class SiteWalker extends RecursiveTask<Void> {
    
    private final IndexingServiceImpl service;
    
    @Getter
    private final Page page;
    private final YAMLConfig config;
    private final Set<String> visitedPages;
    private List<SiteWalker> walkerList;
    
    
    public SiteWalker(Site site, YAMLConfig config, IndexingServiceImpl service) {
        this(new Page("/"), config, service, Collections.synchronizedSet(new HashSet<>()));
        page.setSite(site);
    }
    
    public SiteWalker(Page page, YAMLConfig config, IndexingServiceImpl service) {
        this(page, config, service, new HashSet<>());
    }
    
    private SiteWalker(Page page,
                       YAMLConfig config,
                       IndexingServiceImpl service,
                       Set<String> visitedPages) {
        this.page = page;
        this.config = config;
        this.service = service;
        this.visitedPages = visitedPages;
    }
    
    @Override
    protected Void compute() {
    
        if (Thread.currentThread().isInterrupted()) {
            return null;
        }
        
        if (visitPage()) {
    
            index();
    
            walkerList = new ArrayList<>();
            for (String path : getParsedPages()) {
                Page page = new Page(path);
                page.setSite(this.page.getSite());
                SiteWalker walker = new SiteWalker(page, config, service, visitedPages);
                walker.fork();
                walkerList.add(walker);
            }
            walkerList.forEach(ForkJoinTask::join);
        }
        
        return null;
    }
    
    public void indexOnePage() {
        visitPage();
        index();
    }
    
    private boolean visitPage() {
        
        Site site = page.getSite();
        String path = page.getPath();
        
        synchronized (site) {
            site = service.getSiteService().getSiteByUrl(site.getUrl());
            if (visitedPages.contains(path) || site.getStatus() != Status.INDEXING) {
                return false;
            } else {
                visitedPages.add(path);
                site.updateStatusTime();
                service.getSiteService().saveSite(site);
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
        PageIndexer indexer = new PageIndexer(page, service);
        indexer.index();
    }
    
    private Set<String> getParsedPages() {
        PageParser parser = new PageParser(page);
        Set<String> parsedPages = parser.parseLink();
        parsedPages.removeIf(visitedPages::contains);
        return parsedPages;
    }
    
}
