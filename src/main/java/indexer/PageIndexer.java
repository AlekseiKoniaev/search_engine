package indexer;

import lemmatizer.Lemmatizer;
import org.jsoup.nodes.Document;
import repository.DBConnection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PageIndexer {
    
    private static Map<String, Float> fields;
    
    
    public static void setFields(Map<String, Float> fields) {
        PageIndexer.fields = fields;
    }
    
    
    private final String path;
    private final Document document;
    private Map<String, Map<String, Integer>> lemmas;
    private Set<Index> indexes;
    
    
    public PageIndexer(String path, Document document) {
        this.path = path;
        this.document = document;
        indexes = new HashSet<>();
    }
    
    
    public void index() {
        lemmas = getLemmasForPage();
        addLemmasToDB();
        indexes = createIndexes();
        addIndexesToDB();
    }
    
    private Map<String, Map<String, Integer>> getLemmasForPage() {
        return fields.keySet().stream().collect(Collectors.toMap(
                selector -> selector, this::getLemmasForField, (a, b) -> b));
    }
    
    private Map<String, Integer> getLemmasForField(String selector) {
        String text = extractFragment(selector);
        if (text.isEmpty()) {
            return new HashMap<>();
        }
        Lemmatizer lemmatizer = new Lemmatizer(text);
        return lemmatizer.countLemmas();
    }

    private String extractFragment(String selector) {
        return document.select(selector).text();
    }
    
    
    private void addLemmasToDB() {
        Set<String> uniqueLemmas = lemmas.keySet().stream()
                .flatMap(selector -> lemmas.get(selector).keySet().stream())
                .collect(Collectors.toSet());
        DBConnection.insertLemmas(uniqueLemmas);
    }
    
    
    private Set<Index> createIndexes() {
        Map<String, Float> rankForLemmas = calculateRankForLemmas();
        Set<Index> indexes = new HashSet<>();
        for (String lemma : rankForLemmas.keySet()) {
            int pageId = getPageId(path);
            int lemmaId = getLemmaId(lemma);
            float rank = rankForLemmas.get(lemma);
            Index index = new Index(pageId, lemmaId, rank);
            indexes.add(index);
        }
        return indexes;
    }
    
    private Map<String, Float> calculateRankForLemmas() {
        Map<String, Float> lemmasWeights = new HashMap<>();
        for (String selector : fields.keySet()) {
            float weight = fields.get(selector);
            Map<String, Integer> lemmasForField = lemmas.get(selector);
            for (String lemma : lemmasForField.keySet()) {
                int frequency = lemmasForField.get(lemma);
                float currentRank = weight * frequency;
                lemmasWeights.merge(lemma, currentRank, Float::sum);
            }
        }
        return lemmasWeights;
    }
    
    private int getPageId(String path) {
        return DBConnection.getPageId(path);
    }
    
    private int getLemmaId(String lemma) {
        return DBConnection.getLemmaId(lemma);
    }
    
    
    private void addIndexesToDB() {
        DBConnection.insertIndexes(indexes);
    }
}
