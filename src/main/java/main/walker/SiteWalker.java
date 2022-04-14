package main.walker;

import lombok.Getter;
import main.config.WebConfig;
import main.exceptions.ServerNotFoundException;
import main.indexer.PageIndexer;
import main.model.Page;
import main.model.Site;
import main.service.PageService;
import main.service.SiteService;
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
import java.util.concurrent.RecursiveAction;

@Component
@Scope("prototype")
public class SiteWalker extends RecursiveAction {
    
    public static final int PAGES_VISIT_LIMIT = 500;
    
    private final ApplicationContext applicationContext;
    private final WebConfig config;
    private final PageService pageService;
    private final SiteService siteService;
    
    private Set<String> visitedPages;
    private WalkerExecutor executor;
    
    @Getter
    private Site site;
    private Page page;
    
    
    @Autowired
    public SiteWalker(ApplicationContext applicationContext,
                      WebConfig config,
                      PageService pageService,
                      SiteService siteService) {
        this.applicationContext = applicationContext;
        this.config = config;
        this.pageService = pageService;
        this.siteService = siteService;
    }
    
    public void init(Site site) {
        init(new Page("/"), site);
        page.setSiteId(site.getId());
    }
    
    public void init(Page page, Site site) {
        init(page, site, Collections.synchronizedSet(new HashSet<>()));
    }
    
    private void init(Page page, Site site, Set<String> visitedPages) {
        this.page = page;
        this.site = site;
        this.visitedPages = visitedPages;
    }
    
    void setExecutor(WalkerExecutor executor) {
        this.executor = executor;
    }
    
    @Override
    protected void compute() {
        
        if (executor.isDisableCompute() || visitedPages.size() > PAGES_VISIT_LIMIT) {
            return;
        }
        
        boolean pageVisitingIsOk = false;
        try {
            pageVisitingIsOk = visitPage();
        } catch (ServerNotFoundException e) {
            site.setLastError(e.getMessage());
            site.updateStatusTime();
            siteService.saveSite(site);
        }
        
        if (pageVisitingIsOk) {
    
            index();
    
            List<SiteWalker> walkerList = new ArrayList<>();
            for (String path : getParsedPages()) {
                Page page = new Page(path);
                page.setSiteId(site.getId());
                SiteWalker walker = applicationContext.getBean(SiteWalker.class);
                walker.init(page, site, visitedPages);
                walkerList.add(walker);
                walker.setExecutor(executor);
                walker.fork();
            }
            
            for (SiteWalker walker : walkerList) {
                walker.join();
            }
        }
    }
    
    public void indexOnePage() {
        visitPage();
        index();
    }
    
    private boolean visitPage() {
        
        String path = page.getPath();
        
        synchronized (site) {
            if (visitedPages.contains(path)) {
                return false;
            } else {
                visitedPages.add(path);
                site.updateStatusTime();
                siteService.updateStatusTime(site);
            }
        }
        
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
            throw new ServerNotFoundException(site.getUrl() + page.getPath());
        }
    
        pageService.savePage(page);
        
        
        return page.getCode() == 200;
    }
    
    private void index() {
        PageIndexer indexer = applicationContext.getBean(PageIndexer.class);
        indexer.init(page, site);
        indexer.index();
    }
    
    private Set<String> getParsedPages() {
        PageParser parser = new PageParser(page.getDocument(), site.getUrl());
        Set<String> parsedPages = parser.parseLinks();
        parsedPages.removeIf(visitedPages::contains);
        return parsedPages;
    }
    
}
