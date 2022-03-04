package main.indexer;

import main.lemmatizer.Lemmatizer;
import main.model.Field;
import main.model.Index;
import main.model.Lemma;
import main.model.Page;
import main.service.impl.IndexingServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PageIndexer {
    
    
    private final IndexingServiceImpl indexingService;
    
    private final Page page;
    private List<Field> fields;
    private Map<String, Map<String, Integer>> lemmas;
    private List<Index> indexes;
    
    
    public PageIndexer(Page page, IndexingServiceImpl indexingService) {
        this.indexingService = indexingService;
        this.page = page;
        indexes = new ArrayList<>();
    }
    
    
    public void index() {
        fields = indexingService.getFieldService().getAllFields();
        lemmas = getLemmasForPage();
        addLemmasToDB();
        indexes = createIndexes();
        addIndexesToDB();
    }
    
    private Map<String, Map<String, Integer>> getLemmasForPage() {
        return fields.stream()
                .map(Field::getSelector)
                .collect(Collectors.toMap(
                selector -> selector, this::getLemmasForField, (a, b) -> b));
    }
    
    private Map<String, Integer> getLemmasForField(String selector) {
        String text = extractFragment(selector);
        return new Lemmatizer(text).getLemmas();
    }

    private String extractFragment(String selector) {
        return page.getDocument().select(selector).text();
    }
    
    
    private void addLemmasToDB() {
        List<Lemma> uniqueLemmas = lemmas.keySet().stream()
                .flatMap(selector -> lemmas.get(selector).keySet().stream())
                .distinct()
                .map(lemmaStr -> {
                    Lemma lemma = new Lemma();
                    lemma.setLemma(lemmaStr);
                    lemma.setSite(page.getSite());
                    return lemma;
                })
                .toList();
        synchronized (page.getSite()) {
            indexingService.getLemmaService().saveLemmas(uniqueLemmas);
        }
    }
    
    
    private List<Index> createIndexes() {
        Map<String, Float> rankForLemmas = calculateRankForLemmas();
        List<Index> indexes = new ArrayList<>();
        for (String lemmaStr : rankForLemmas.keySet()) {
            Page page = getPage(this.page.getPath());
            Lemma lemma = getLemma(lemmaStr);
            float rank = rankForLemmas.get(lemmaStr);
            
            Index index = new Index();
            index.setPage(page);
            index.setLemma(lemma);
            index.setRank(rank);
            
            indexes.add(index);
        }
        return indexes;
    }
    
    private Map<String, Float> calculateRankForLemmas() {
        Map<String, Float> lemmasWeights = new HashMap<>();
        for (Field field : fields) {
            
            float weight = field.getWeight();
            Map<String, Integer> lemmasForField = lemmas.get(field.getSelector());
            
            for (String lemmaStr : lemmasForField.keySet()) {
                int frequency = lemmasForField.get(lemmaStr);
                float currentRank = weight * frequency;
                lemmasWeights.merge(lemmaStr, currentRank, Float::sum);
            }
        }
        return lemmasWeights;
    }
    
    private Page getPage(String path) {
        return indexingService.getPageService().getPageByPath(path);
    }
    
    private Lemma getLemma(String lemma) {
        return indexingService.getLemmaService().getLemmaByLemma(lemma);
    }
    
    
    private void addIndexesToDB() {
        synchronized (page.getSite()) {
            indexingService.getIndexService().saveIndexes(indexes);
        }
    }
}
