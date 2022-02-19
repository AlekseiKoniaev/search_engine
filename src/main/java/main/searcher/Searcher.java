package main.searcher;

import main.lemmatizer.Lemmatizer;
import main.model.Finding;
import main.model.Index;
import main.model.Lemma;
import main.model.Page;
import org.jsoup.nodes.Document;
import main.repository.DBConnection;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Searcher {
    
    private Map<String, Float> fields;
    private Set<Lemma> lemmas;
    private Set<Index> indexes;
    private Set<Page> pages;
    
    public Set<Finding> search(String query) {
        fields = DBConnection.getFields();
        Lemmatizer lemmatizer = new Lemmatizer(query);
        Set<String> lemmasStr = lemmatizer.getLemmas().keySet();
        lemmas = getLemmas(lemmasStr);
        
        indexes = findIndexes();
        pages = getPages();
    
        Map<Page, Float> pagesAbsRelevance = calculateAbsRelevance();
        Map<Page, Float> pagesRelRelevance = calculateRelRelevance(pagesAbsRelevance);
    
        return createFindings(pagesRelRelevance);
    }
    
    
    private Set<Lemma> getLemmas(Set<String> lemmasStr) {
        int thresholdCountPages = (int) (DBConnection.getPageCount() * 0.5);
        return DBConnection.getLemmas(lemmasStr).stream()
                .filter(lemma -> lemma.getFrequency() < thresholdCountPages)
                .collect(Collectors.toSet());
    }
    
    private Set<Index> findIndexes() {
        Set<Index> indexes = new HashSet<>();
        for (Lemma lemma : lemmas) {
            Set<Index> foundIndexes = DBConnection.findIndexes(lemma.getId(), "_lemma_id");
            if (indexes.isEmpty()) {
                indexes.addAll(foundIndexes);
            } else {
                leaveForIdenticalPages(indexes, foundIndexes);
                addForIdenticalPages(indexes, foundIndexes);
            }
        }
        return indexes;
    }
    
    private void leaveForIdenticalPages(Set<Index> indexes, Set<Index> foundIndexes) {
        indexes.removeIf(index -> {
                    for (Index foundIndex : foundIndexes) {
                        if (foundIndex.getPageId() == index.getPageId()) {
                            return false;
                        }
                    }
                    return true;
                }
        );
    }
    
    private void addForIdenticalPages(Set<Index> indexes, Set<Index> foundIndexes) {
        Set<Index> set = new HashSet<>();
        for (Index foundIndex : foundIndexes) {
            for (Index index : indexes) {
                if (index.getPageId() == foundIndex.getPageId()) {
                    set.add(foundIndex);
                    break;
                }
            }
        }
        indexes.addAll(set);
    }
    
    private Set<Page> getPages() {
        Set<Page> foundPages = new HashSet<>();
        foundPages = indexes.stream()
                .map(Index::getPageId)
                .distinct()
                .map(DBConnection::getPage)
                .collect(Collectors.toSet());
        return foundPages;
    }
    
    private Map<Page, Float> calculateAbsRelevance() {
        Map<Page, Float> pagesAbsRelevance = new HashMap<>();
        for (Page page : pages) {
            int pageId = page.getId();
            float absRelevance = 0.0f;
            for (Index index : indexes) {
                if (index.getPageId() == pageId) {
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
        
        for (String field : fields.keySet()) {
            String fieldText = document.select(field).text();
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