package main.service.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.api.response.ErrorResponse;
import main.api.response.Response;
import main.api.response.StatResponse;
import main.api.response.model.SiteInfo;
import main.config.YAMLConfig;
import main.model.Index;
import main.model.Lemma;
import main.model.Page;
import main.model.Site;
import main.service.FieldService;
import main.service.IndexService;
import main.service.IndexingService;
import main.service.LemmaService;
import main.service.PageService;
import main.service.SiteService;
import main.walker.SiteWalker;
import main.walker.WalkerExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static main.model.enums.Status.INDEXED;
import static main.model.enums.Status.INDEXING;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    
    private static final String INDEXING_RUN = "Индексация уже запущена";
    private static final String INDEXING_NOT_RUN = "Индексация не запущена";
    private static final String INDEXING_ENDS = "Индексация завершается";
    private static final String SITE_NOT_FOUND = "Данная страница находится за " +
            "пределами сайтов, указанных в конфигурационном файле";
    
    @Autowired
    private YAMLConfig config;
    
    @Autowired
    @Getter
    private FieldService fieldService;
    
    @Autowired
    @Getter
    private PageService pageService;
    
    @Autowired
    @Getter
    private LemmaService lemmaService;
    
    @Autowired
    @Getter
    private IndexService indexService;
    
    @Autowired
    @Getter
    private SiteService siteService;
    
    
    private WalkerExecutor walkerExecutor;
    private ForkJoinPool pool;

    
    @Override
    public Response startIndexing() {
        
        List<Site> sites = getSites();
        
        if (sites.stream().anyMatch(site -> (site.getStatus() == INDEXING))) {
            return new ErrorResponse(INDEXING_RUN);
        }
        
        List<SiteWalker> walkers = sites.stream().map(this::prepareToIndexing).toList();
        walkerExecutor = new WalkerExecutor(walkers, this);
        pool = new ForkJoinPool();
        pool.execute(walkerExecutor);
        
        return new Response();
    }
    
    @Override
    public Response stopIndexing() {
    
        if (pool.isTerminated()) {
            return new ErrorResponse(INDEXING_NOT_RUN);
        } else if (pool.isTerminating()) {
            return new ErrorResponse(INDEXING_ENDS);
        }
        
        boolean result = walkerExecutor.stopIndexing(pool);
        
        return new Response(result);
    }
    
    @Override
    public Response indexPage(String url) {
        
        Site site = getSites().stream()
                .filter(s -> url.startsWith(s.getUrl()))
                .findFirst()
                .orElse(null);
        
        if (site == null) {
            return new ErrorResponse(SITE_NOT_FOUND);
        } else if (site.getStatus() == INDEXING) {
            return new ErrorResponse(INDEXING_RUN);
        }
        
        String path = url.replaceAll(site.getUrl(), "");
        path = path.isEmpty() ? "/" : path;
        
        Page page = new Page(path);
        page.setSite(site);
        SiteWalker walker = prepareToIndexing(page);
        walker.indexOnePage();
        
        page.getSite().setStatus(INDEXED);
        siteService.saveSite(site);
    
        return new Response();
    }
    
    @Override
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
            info.setPages(pageService.countForSite(site));
            info.setLemmas(lemmaService.countForSite(site));
            
            detailed.add(info);
        }
        
        return new StatResponse(detailed);
    }
    
    
    private List<Site> getSites() {
        return config.getSites().stream()
                .map(site -> {
                    Site currentSite = siteService.getSiteByUrl(site.getUrl());
                    return currentSite == null ? site : currentSite;
                })
                .collect(Collectors.toList());
    }
    
    private SiteWalker prepareToIndexing(Site site) {
        
        if (site.getStatus() == INDEXED) {
            removeDataForSite(site);
        }
    
        switchStatus(site);
    
        return new SiteWalker(site, config, this);
    }
    
    private SiteWalker prepareToIndexing(Page page) {
        
        Site site = page.getSite();
        
        if (site.getStatus() == INDEXED) {
            removeDataForPage(page);
        }
    
        switchStatus(site);
    
        return new SiteWalker(page, config, this);
    }
    
    @Transactional
    private void switchStatus(Site site) {
        synchronized (site) {
            site.setStatus(INDEXING);
            siteService.saveSite(site);
        }
    }
    
    private void removeDataForSite(Site site) {
        List<Page> foundPages = pageService.getPagesBySiteId(site.getId());
        indexService.deleteByPages(foundPages);
        pageService.deleteBySite(site);
        lemmaService.deleteBySite(site);
    }
    
    private void removeDataForPage(Page page) {
        
        Page foundPage = pageService.getPageBySiteAndPath(page.getSite(), page.getPath());
        
        if (foundPage == null) {
            return;
        }
        
        List<Index> foundIndexes = indexService.findIndexesByPageId(foundPage.getId());
        List<Lemma> foundLemmas = foundIndexes.stream()
                .map(index -> lemmaService.getLemmaById(index.getLemma().getId()))
                .distinct()
                .toList();
        
        indexService.deleteByPage(foundPage);
        pageService.deleteBySiteAndPath(page.getSite(), page.getPath());
        foundLemmas.forEach(lemma -> lemmaService.decrementAndUpdateLemma(lemma));
    }
    
}
