package main.searcher;

import lombok.Getter;
import main.api.request.model.SearchQuery;
import main.api.response.model.Finding;
import main.api.response.model.FoundPageObject;
import main.lemmatizer.Lemmatizer;
import main.model.Site;
import main.searcher.enums.SearchStatus;
import main.service.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SearchQueryHandler {
    
    private final ApplicationContext applicationContext;
    private final SiteService siteService;
    
    @Getter
    private SearchStatus status;
    private SearchQuery query;
    private Set<String> uniqueLemmas;
    private List<FoundPageObject> foundPageObjectList;
    private List<Site> sites;
    
    
    @Autowired
    public SearchQueryHandler(ApplicationContext applicationContext, SiteService siteService) {
        this.applicationContext = applicationContext;
        this.siteService = siteService;
    }
    
    
    public void search(SearchQuery query) {
        
        status = SearchStatus.READY;
        
        if (this.query == null || !this.query.equals(query)) {
            
            this.query = query;
            
            uniqueLemmas = getUniqueLemmas();
            if (uniqueLemmas.isEmpty()) {
                status = SearchStatus.WRONG_QUERY;
                return;
            }
            
            List<Searcher> searchers = getSearcherList();
            foundPageObjectList = getFoundPageObjectList(searchers);
            if (foundPageObjectList.isEmpty()) {
                status = SearchStatus.NOT_FOUND;
                return;
            }
            
        } else {
            this.query = query;
        }
        
        status = SearchStatus.OK;
    }
    
    private Set<String> getUniqueLemmas() {
        return new Lemmatizer(query.getQuery()).getLemmas().keySet();
    }
    
    private List<Searcher> getSearcherList() {
        
        if (sites == null || sites.isEmpty()) {
            sites = siteService.getAllSites();
        }
        
        List<Searcher> searchers = new ArrayList<>();
        if (query.getSiteUrl().equals("")) {
            sites.forEach(site -> createSearcher(site, searchers));
        } else {
            Site site = siteService.getSiteByUrl(query.getSiteUrl());
            createSearcher(site, searchers);
        }
        
        return searchers;
    }
    
    private void createSearcher(Site site, List<Searcher> searchers) {
        Searcher searcher = applicationContext.getBean(Searcher.class);
        searcher.init(uniqueLemmas, site.getId());
        searchers.add(searcher);
    }
    
    private List<FoundPageObject> getFoundPageObjectList(List<Searcher> searchers) {
        List<FoundPageObject> foundPageObjectList = searchers.stream()
                .flatMap(s -> s.getPageAbsRelevanceList().stream())
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        
        if (foundPageObjectList.isEmpty()) {
            return foundPageObjectList;
        }
        
        float maxRelevance = foundPageObjectList.get(0).getRelevance();
        
        foundPageObjectList.forEach(pr -> pr.setRelevance(pr.getRelevance() / maxRelevance));
        
        return foundPageObjectList;
    }
    
    public List<Finding> getFindings() {
        FindingsCreator creator = applicationContext.getBean(FindingsCreator.class);
        creator.init(query, foundPageObjectList);
        return creator.getFindings();
    }
    
    public int getCount() {
        return foundPageObjectList.size();
    }
    
}
