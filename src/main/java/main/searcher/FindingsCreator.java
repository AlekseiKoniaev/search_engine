package main.searcher;

import main.api.request.model.SearchQuery;
import main.api.response.model.Finding;
import main.api.response.model.FoundPageObject;
import main.model.Field;
import main.model.Lemma;
import main.model.Page;
import main.model.Site;
import main.service.FieldService;
import main.service.PageService;
import main.service.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class FindingsCreator {

    private final ApplicationContext applicationContext;
    private final FieldService fieldService;
    private final PageService pageService;
    private final SiteService siteService;
    
    private SearchQuery query;
    private List<FoundPageObject> foundPageObjectList;
    
    @Autowired
    public FindingsCreator(ApplicationContext applicationContext,
                           FieldService fieldService,
                           PageService pageService,
                           SiteService siteService) {
        this.applicationContext = applicationContext;
        this.fieldService = fieldService;
        this.pageService = pageService;
        this.siteService = siteService;
    }
    
    public void init(SearchQuery query, List<FoundPageObject> foundPageObjectList) {
        this.query = query;
        this.foundPageObjectList = foundPageObjectList;
    }
    
    
    public List<Finding> getFindings() {
        
        List<FoundPageObject> foundPageObjectSublist = getFoundPageObjectSublist();
        Map<Integer, Page> pageMap = getPageMap(foundPageObjectSublist);
        
        return createFindings(foundPageObjectSublist, pageMap);
    
    }
    
    private List<FoundPageObject> getFoundPageObjectSublist() {
        return foundPageObjectList.stream()
                .skip(query.getOffset())
                .limit(query.getLimit())
                .collect(Collectors.toList());
    }
    
    private Map<Integer, Page> getPageMap(List<FoundPageObject> foundPageObjectSublist) {
        
        List<Integer> pageIdList = foundPageObjectSublist.stream()
                .map(FoundPageObject::getPageId)
                .collect(Collectors.toList());
        
        List<Page> pages = pageService.getPagesByIds(pageIdList);
        
        return pages.stream().collect(Collectors.toMap(Page::getId, Function.identity()));
    }
    
    private List<Finding> createFindings(List<FoundPageObject> foundPageObjectSublist,
                                         Map<Integer, Page> pageMap) {
        
        return foundPageObjectSublist.stream()
                .map(fpo -> {
                    Finding finding = new Finding();
                    
                    Page page = pageMap.get(fpo.getPageId());
                    Site site = siteService.getSiteById(page.getSiteId()); // todo : create cash with List<Site>
                    
                    finding.setSite(site.getUrl());
                    finding.setSiteName(site.getName());
                    finding.setUri(page.getPath());
                    finding.setTitle(page.getDocument().title());
                    finding.setSnippet(createSnippet(page, fpo.getLemmas()));
                    finding.setRelevance(fpo.getRelevance());
                    
                    return finding;
                })
                .sorted()
                .collect(Collectors.toList());
    }
    
    private String createSnippet(Page page, List<Lemma> lemmas) {
        List<Field> fields = fieldService.getAllFields();
        SnippetCreator creator = new SnippetCreator(fields, page, lemmas);
        return creator.getSnippet();
    }
}
