package main.searcher;

import main.lemmatizer.Lemmatizer;
import main.model.Field;
import main.model.Finding;
import main.model.Index;
import main.model.Lemma;
import main.model.Page;
import main.repository.HibernateConnection;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Searcher {
    
    private List<Field> fields;
    private List<Lemma> lemmas;
    private List<Index> indexes;
    private List<Page> pages;
    
    public Set<Finding> search(String query) {
        fields = HibernateConnection.getFields();
        Lemmatizer lemmatizer = new Lemmatizer(query);
        Set<String> lemmasStr = lemmatizer.getLemmas().keySet();
        lemmas = getLemmas(lemmasStr);
        
        indexes = findIndexes();
        pages = getPages();
    
        Map<Page, Float> pagesAbsRelevance = calculateAbsRelevance();
        Map<Page, Float> pagesRelRelevance = calculateRelRelevance(pagesAbsRelevance);
    
        return createFindings(pagesRelRelevance);
    }
    
    
    private List<Lemma> getLemmas(Set<String> lemmasStr) {
        int thresholdCountPages = (int) (HibernateConnection.getPageCount() * 0.5);
        return HibernateConnection.getLemmas(lemmasStr).stream()
                .filter(lemma -> lemma.getFrequency() < thresholdCountPages)
                .collect(Collectors.toList());
    }
    
    private List<Index> findIndexes() {
        List<Index> indexes = new ArrayList<>();
        for (Lemma lemma : lemmas) {
            List<Index> foundIndexes = HibernateConnection.findIndexes(lemma.getId(), "lemma_id");
            if (indexes.isEmpty()) {
                indexes.addAll(foundIndexes);
            } else {
                leaveForIdenticalPages(indexes, foundIndexes);
                addForIdenticalPages(indexes, foundIndexes);
            }
        }
        return indexes;
    }
    
    private void leaveForIdenticalPages(List<Index> indexes, List<Index> foundIndexes) {
        indexes.removeIf(index -> foundIndexes.stream()
                .noneMatch(foundIndex -> foundIndex.getId() == index.getId()));
    }
    
    private void addForIdenticalPages(List<Index> indexes, List<Index> foundIndexes) {
        List<Index> list = foundIndexes.stream()
                .filter(foundIndex -> indexes.stream()
                        .anyMatch(index -> index.getId() == foundIndex.getId()))
                .toList();
        indexes.addAll(list);
    }
    
    private List<Page> getPages() {
        return indexes.stream()
                .map(Index::getPage)
                .distinct()
                .collect(Collectors.toList());
    }
    
    private Map<Page, Float> calculateAbsRelevance() {
        Map<Page, Float> pagesAbsRelevance = new HashMap<>();
        for (Page page : pages) {

            float absRelevance = 0.0f;
            for (Index index : indexes) {
                if (page.equals(index.getPage())) {
                    absRelevance += index.getRank();
                }
            }
            pagesAbsRelevance.put(page, absRelevance);
        }
        return pagesAbsRelevance;
    }
    
    private Map<Page, Float> calculateRelRelevance(Map<Page, Float> pagesAbsRelevance) {
        Map<Page, Float> pagesRelRelevance = new HashMap<>();
        Float maxRelevance = (Float) pagesAbsRelevance
                .values()
                .stream()
                .collect(TreeSet::new, TreeSet::add, TreeSet::addAll)
                .last();    // todo : проверить на максимальное значение
        
        for (Page page : pagesAbsRelevance.keySet()) {
            Float absRelevance = pagesAbsRelevance.get(page);
            Float relRelevance = absRelevance / maxRelevance;
            pagesRelRelevance.put(page, relRelevance);
        }
        return pagesRelRelevance;
    }
    
    private Set<Finding> createFindings(Map<Page, Float> pagesRelRelevance) {
        Set<Finding> findings = new TreeSet<>();
        for (Page page : pagesRelRelevance.keySet()) {
            Finding finding = new Finding();
            
            finding.setUri(page.getPath());
            
            String title = page.getDocument().title();
            finding.setTitle(title);
            
            String snippet = createSnippet(page);
            finding.setSnippet(snippet);
            
            float relevance = pagesRelRelevance.get(page);
            finding.setRelevance(relevance);
            
            findings.add(finding);
        }
        return findings;
    }
    
    private String createSnippet(Page page) {
        StringBuilder snippet = new StringBuilder();
        Document document = page.getDocument();
        
        for (Field field : fields) {
            String selector = field.getSelector();
            String fieldText = document.select(selector).text();
            String fragment = formatFragment(fieldText);
            snippet.append(fragment).append("\t\n");
        }
        
        return snippet.toString();
    }
    
    private String formatFragment(String text) {
        StringBuilder fragment = new StringBuilder(text);
        Map<Integer, Integer> matchIndexMap = getMatchIndexMap(text);
        for (Integer begin : matchIndexMap.keySet()) {
            Integer end = matchIndexMap.get(begin);
            fragment.insert(end, "</b>");
            fragment.insert(begin, "<b>");
        }
        optimiseLength(fragment);
        return fragment.toString();
    }
    
    private Map<Integer, Integer> getMatchIndexMap(String text) {
        Map<Integer, Integer> matchIndexMap = new TreeMap<>(Comparator.reverseOrder());
        text = text.toLowerCase();
        Map<String, List<String>> initialForms = new Lemmatizer(text).getInitialForms();
        for (Lemma lemma : lemmas) {
            List<String> initialWords = initialForms.get(lemma.getLemma());
            if (initialWords == null) {
                continue;
            }
            for (String initialWord : initialWords) {
                int begin = text.indexOf(initialWord);
                while (begin > -1) {
                    char[] endCharacters = {' ', '.', ',', ';', ':', '\'', '"', '?', '!', '%', ')'};
                    int end = text.length();
                    for (char endChar : endCharacters) {
                        int index = text.indexOf(endChar, begin);
                        if (index > begin && index < end) {
                            end = index;
                        }
                    }
                    matchIndexMap.put(begin, end);
                    begin = text.indexOf(initialWord, end);
                }
            }
        }
        return matchIndexMap;
    }
    
    private void optimiseLength(StringBuilder fragment) {
        Map<Integer, Integer> tagIndexMap = getTagIndexMap(fragment);
        int finish = fragment.length();
        for (Integer begin : tagIndexMap.keySet()) {
            int end = tagIndexMap.get(begin) + 4;
            int diff = finish - end;
            if (diff > 150) {
                trimFragment(fragment, end, finish);
            }
            finish = begin;
        }
        if (finish > 100) {
            trimFragment(fragment, 0, finish);
        }
    }
    
    private Map<Integer, Integer> getTagIndexMap(StringBuilder fragment) {
        Map<Integer, Integer> tagIndexMap = new TreeMap<>(Comparator.reverseOrder());
        int begin;
        int end = 0;
        for (;;) {
            begin = fragment.indexOf("<b>", end);
            if (begin == -1) {
                break;
            }
            end = fragment.indexOf("</b>", begin);
            tagIndexMap.put(begin, end);
        }
        return tagIndexMap;
    }
    
    private void trimFragment(StringBuilder fragment, int begin, int end) {
        int beginDel;
        int endDel;
        if (end == fragment.length()) {
            beginDel = fragment.indexOf(" ", begin + 50);
            endDel = end;
        } else if (begin == 0) {
            beginDel = 0;
            endDel = fragment.indexOf(" ", end - 60);
        } else {
            beginDel = fragment.indexOf(" ", begin + 50);
            endDel = fragment.indexOf(" ", end - 60);
        }
        fragment.replace(beginDel, endDel, "...");
    }
    
}