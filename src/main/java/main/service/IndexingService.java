package main.service;

import main.api.response.ErrorResponse;
import main.api.response.Response;
import main.api.response.StatResponse;
import main.api.response.model.SiteInfo;
import main.config.WebConfig;
import main.model.Index;
import main.model.Lemma;
import main.model.Page;
import main.model.Site;
import main.walker.SiteWalker;
import main.walker.WalkerExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static main.model.enums.Status.INDEXED;
import static main.model.enums.Status.INDEXING;

@Service
public class IndexingService {
    
    private static final String INDEXING_RUN = "Индексация уже запущена";
    private static final String INDEXING_NOT_RUN = "Индексация не запущена";
    private static final String INDEXING_ENDS = "Индексация завершается";
    private static final String SITE_NOT_FOUND = "Данная страница находится за " +
            "пределами сайтов, указанных в конфигурационном файле";
    
    private final ApplicationContext applicationContext;
    private final WebConfig config;
    
    private final PageService pageService;
    private final LemmaService lemmaService;
    private final IndexService indexService;
    private final SiteService siteService;
    
    private WalkerExecutor walkerExecutor;
    private ForkJoinPool pool;
    
    @Autowired
    public IndexingService(ApplicationContext applicationContext,
                           WebConfig config,
                           PageService pageService,
                           LemmaService lemmaService,
                           IndexService indexService,
                           SiteService siteService) {
        this.applicationContext = applicationContext;
        this.config = config;
        this.pageService = pageService;
        this.lemmaService = lemmaService;
        this.indexService = indexService;
        this.siteService = siteService;
    }
    
    
    public Response startIndexing() {
        
        List<Site> sites = getAllSites();
        
        if (sites.stream().anyMatch(site -> (site.getStatus() == INDEXING))) {
            return new ErrorResponse(INDEXING_RUN);
        }
        
        List<SiteWalker> walkers = sites.stream().map(this::prepareToIndexing).collect(Collectors.toList());
        pool = new ForkJoinPool();
        
        walkerExecutor = applicationContext.getBean(WalkerExecutor.class);
        walkerExecutor.init(walkers, pool);
        walkerExecutor.reinitialize();
        
        pool.execute(walkerExecutor);
        
        return new Response();
    }
    
    public Response stopIndexing() {
    
        if (pool.isTerminated()) {
            return new ErrorResponse(INDEXING_NOT_RUN);
        } else if (pool.isTerminating()) {
            return new ErrorResponse(INDEXING_ENDS);
        }
        
        boolean result = walkerExecutor.stopIndexing(pool);
        
        return new Response(result);
    }
    
    public Response indexPage(String url) {
        
        Site site = getAllSites().stream()
                .filter(s -> url.startsWith(s.getUrl()))
                .findFirst()
                .orElse(null);
        
        if (site == null) {
            return new ErrorResponse(SITE_NOT_FOUND);
        } else if (site.getStatus() == INDEXING) {
            return new ErrorResponse(INDEXING_RUN);
        }
        
        ForkJoinPool.commonPool().execute(() -> {
            String path = url.replaceAll(site.getUrl(), "");
            path = path.isEmpty() ? "/" : path;
    
            Page page = new Page(path);
            page.setSiteId(site.getId());
            SiteWalker walker = prepareToIndexing(page, site);
            walker.indexOnePage();
    
            site.setStatus(INDEXED);
            siteService.updateStatus(site);
        });
    
        return new Response();
    }
    
    public StatResponse statistics() {
        List<SiteInfo> detailed = new ArrayList<>();
        
        List<Site> sites = siteService.getAllSites();
        for (Site site : sites) {
            SiteInfo info = new SiteInfo();
            
            info.setUrl(site.getUrl());
            info.setName(site.getName());
            info.setStatus(site.getStatus().toString());
            info.setStatusTime(site.getStatusTime().atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli());
            info.setError(site.getLastError());
            info.setPages(pageService.countBySiteId(site.getId()));
            info.setLemmas(lemmaService.countBySiteId(site.getId()));
            
            detailed.add(info);
        }
        
        return new StatResponse(detailed);
    }
    
    
    private List<Site> getAllSites() {
        List<Site> collect = new ArrayList<>();
        
        List<Site> sites = config.getSites();
        
        for (Site site : sites) {
            Site currentSite = siteService.getSiteByUrl(site.getUrl());
            if (currentSite == null) {
                site.setStatus(INDEXED);
                siteService.saveSite(site);
                currentSite = siteService.getSiteByUrl(site.getUrl());
            }
            collect.add(currentSite);
        }
        
        return collect;
    }
    
    private SiteWalker prepareToIndexing(Site site) {
        
        if (site.getStatus() == INDEXED) {
            removeDataForSite(site);
        }
    
        switchStatus(site);
    
        SiteWalker walker = applicationContext.getBean(SiteWalker.class);
        walker.init(site);
        
        return walker;
    }
    
    private SiteWalker prepareToIndexing(Page page, Site site) {
        
        if (site.getStatus() == INDEXED) {
            removeDataForPage(page);
        }
    
        switchStatus(site);
    
        SiteWalker walker = applicationContext.getBean(SiteWalker.class);
        walker.init(page, site);
    
        return walker;
    }
    
    private void removeDataForSite(Site site) {
        pageService.deleteBySiteId(site.getId());
        lemmaService.deleteBySiteId(site.getId());
    }
    
    private void removeDataForPage(Page page) {
        
        Page foundPage = pageService.getPageByPathAndSiteId(page.getPath(), page.getSiteId());
        
        if (foundPage == null) {
            return;
        }
        
        List<Index> foundIndexes = indexService.findIndexesByPageId(foundPage.getId());
        List<Lemma> foundLemmas = foundIndexes.stream()
                .map(index -> lemmaService.getLemmaById(index.getLemmaId()))
                .distinct()
                .collect(Collectors.toList());
        
        pageService.deleteByPathAndSiteId(page.getPath(), page.getSiteId());
        foundLemmas.forEach(lemmaService::decrementAndUpdateLemma);
    }
    
    private void switchStatus(Site site) {
        synchronized (site) {
            site.setStatus(INDEXING);
            siteService.updateStatus(site);
        }
    }
    
}