package main.indexer;

import main.lemmatizer.Lemmatizer;
import main.model.Field;
import main.model.Index;
import main.model.Lemma;
import main.model.Page;
import main.repository.HibernateConnection;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PageIndexer {
    
    private static List<Field> fields;
    
    
    public static void setFields(List<Field> fields) {
        PageIndexer.fields = fields;
    }
    
    
    private final String path;
    private final Document document;
    private Map<String, Map<String, Integer>> lemmas;
    private List<Index> indexes;
    
    
    public PageIndexer(Page page) {
        this.path = page.getPath();
        this.document = page.getDocument();
        indexes = new ArrayList<>();
    }
    
    
    public void index() {
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
        return document.select(selector).text();
    }
    
    
    private void addLemmasToDB() {
        List<Lemma> uniqueLemmas = lemmas.keySet().stream()
                .flatMap(selector -> lemmas.get(selector).keySet().stream())
                .distinct()
                .map(lemmaStr -> {
                    Lemma lemma = new Lemma();
                    lemma.setLemma(lemmaStr);
                    return lemma;
                })
                .toList();
        HibernateConnection.insertLemmas(uniqueLemmas);
    }
    
    
    private List<Index> createIndexes() {
        Map<String, Float> rankForLemmas = calculateRankForLemmas();
        List<Index> indexes = new ArrayList<>();
        for (String lemmaStr : rankForLemmas.keySet()) {
            Page page = getPage(path);
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
        return HibernateConnection.getPageByPath(path);
    }
    
    private Lemma getLemma(String lemma) {
        return HibernateConnection.getLemmaByLemma(lemma);
    }
    
    
    private void addIndexesToDB() {
        HibernateConnection.insertIndexes(indexes);
    }
}
