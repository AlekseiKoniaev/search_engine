package main.searcher;

import main.api.response.model.FoundPageObject;
import main.model.Index;
import main.model.Lemma;
import main.service.IndexService;
import main.service.LemmaService;
import main.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class Searcher {
    
    private static final float THRESHOLD = 1.0f;
    
    private final PageService pageService;
    private final LemmaService lemmaService;
    private final IndexService indexService;

    private Set<String> uniqueLemmas;
    private int siteId;
    
    @Autowired
    public Searcher(PageService pageService,
                    LemmaService lemmaService,
                    IndexService indexService) {
        this.pageService = pageService;
        this.lemmaService = lemmaService;
        this.indexService = indexService;
    }
    
    public void init(Set<String> uniqueLemmas, int siteId) {
        this.uniqueLemmas = uniqueLemmas;
        this.siteId = siteId;
    }
    
    public List<FoundPageObject> getPageAbsRelevanceList() {
        
        Map<Integer, Lemma> lemmaMap = findLemmas(new ArrayList<>(uniqueLemmas));
        
        List<Index> indexes = findIndexes(lemmaMap);
    
        Map<Integer, FoundPageObject> foundPageObjectMap = createFoundPageObjectMap(indexes);
        associateLemmasWithObjects(foundPageObjectMap, lemmaMap);
        calculateAbsRelevance(foundPageObjectMap);
        
        return new ArrayList<>(foundPageObjectMap.values());
    }
    
    private Map<Integer, Lemma> findLemmas(List<String> lemmasStr) {
        
        int thresholdCountPages = (int) (pageService.countBySiteId(siteId) * THRESHOLD);
        
        List<Lemma> lemmaList = lemmaService.getLemmasByLemmasAndSiteId(lemmasStr, siteId);
        
        return lemmaList.size() < lemmasStr.size() ? new HashMap<>() :
                lemmaList.stream()
                .filter(lemma -> lemma.getFrequency() < thresholdCountPages)
                .collect(Collectors.toMap(Lemma::getId, Function.identity()));
        
    }
    
    private List<Index> findIndexes(Map<Integer, Lemma> lemmaMap) {
        
        Map<Integer, List<Index>> groupedIndexes = new HashMap<>();
        
        List<Lemma> lemmaList = lemmaMap.values().stream()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        
        for (Lemma lemma : lemmaList) {
            List<Index> foundIndexes = indexService.findIndexesByLemmaId(lemma.getId());
            Map<Integer, List<Index>> groupedFoundIndexes = foundIndexes.stream()
                    .collect(Collectors.groupingBy(Index::getPageId));
            if (groupedIndexes.isEmpty()) {
                groupedIndexes.putAll(groupedFoundIndexes);
            } else {
                mergeIndexes(groupedIndexes, groupedFoundIndexes);
            }
        }
        
        return groupedIndexes.keySet().stream()
                .flatMap(pageId -> groupedIndexes.get(pageId).stream())
                .collect(Collectors.toList());
    }
    
    private void mergeIndexes(Map<Integer, List<Index>> groupedIndexes,
                              Map<Integer, List<Index>> groupedFoundIndexes) {
        
        List<Integer> pageIdList = new ArrayList<>(groupedIndexes.keySet());
        for (Integer pageId : pageIdList) {
            if (groupedFoundIndexes.containsKey(pageId)) {
                groupedIndexes.get(pageId).addAll(groupedFoundIndexes.get(pageId));
            } else {
                groupedIndexes.remove(pageId);
            }
        }
    }
    
    private Map<Integer, FoundPageObject> createFoundPageObjectMap(List<Index> indexes) {
        
        Map<Integer, FoundPageObject> foundPageObjectMap = new HashMap<>();
    
        for (Index index : indexes) {
        
            int pageId = index.getPageId();
        
            if (foundPageObjectMap.containsKey(pageId)) {
                FoundPageObject foundPageObject = foundPageObjectMap.get(pageId);
                foundPageObject.getIndexes().add(index);
            } else {
                FoundPageObject foundPageObject = new FoundPageObject();
            
                foundPageObject.setPageId(pageId);
                foundPageObject.getIndexes().add(index);
                
                foundPageObjectMap.put(index.getPageId(), foundPageObject);
            }
        }
        
        return foundPageObjectMap;
    }
    
    private void associateLemmasWithObjects(Map<Integer, FoundPageObject> foundPageObjectMap,
                                            Map<Integer, Lemma> lemmaMap) {
        
        foundPageObjectMap.values().forEach(fpo -> {
            List<Lemma> lemmas = fpo.getLemmas();
            List<Index> pageIndexes = fpo.getIndexes();
            pageIndexes.forEach(index -> lemmas.add(lemmaMap.get(index.getLemmaId())));
        });
    }
    
    private void calculateAbsRelevance(Map<Integer, FoundPageObject> foundPageObjectMap) {
        
        foundPageObjectMap.values().forEach(fpo -> {
            float absRelevance = fpo.getIndexes().stream()
                    .map(Index::getRank)
                    .reduce(Float::sum)
                    .orElse(0.0f);
            fpo.setRelevance(absRelevance);
        });
    }
    
}
