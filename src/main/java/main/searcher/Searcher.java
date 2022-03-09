package main.searcher;

import lombok.Getter;
import main.api.response.model.Finding;
import main.lemmatizer.Lemmatizer;
import main.model.Field;
import main.model.Index;
import main.model.Lemma;
import main.model.Page;
import main.model.Site;
import main.searcher.enums.SearchStatus;
import main.service.impl.SearchServiceImpl;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Searcher {
    
    @Getter
    private SearchStatus status;
    @Getter
    private int count;
    @Getter
    private List<Finding> searchResult;
    
    private final SearchServiceImpl searchService;
    private List<Field> fields;
    private List<Lemma> lemmas;
    private List<Index> indexes;
    private List<Page> pages;
    
    public Searcher(SearchServiceImpl searchService) {
        this.searchService = searchService;
    }
    
    public void search(String query, String siteUrl, int offset, int limit) {
        
        status = SearchStatus.READY;
        
        Site site = siteUrl.equals("") ? null : searchService.getSiteService().getSiteByUrl(siteUrl);
        
        fields = searchService.getFieldService().getAllFields();
        Lemmatizer lemmatizer = new Lemmatizer(query);
        Set<String> lemmasStr = lemmatizer.getLemmas().keySet();
        
        lemmas = getLemmas(lemmasStr, site);
        if (lemmas.isEmpty()) {
            status = SearchStatus.WRONG_QUERY;
            return;
        }
        
        indexes = findIndexes();
        if (indexes.isEmpty()) {
            status = SearchStatus.NOT_FOUND;
            return;
        }
        
        pages = getPages();
    
        Map<Page, Float> pagesAbsRelevance = calculateAbsRelevance();
        Map<Page, Float> pagesRelRelevance = calculateRelRelevance(pagesAbsRelevance);
    
        searchResult = createFindings(pagesRelRelevance, offset, limit);
        
        status = SearchStatus.OK;
    }
    
    
    private List<Lemma> getLemmas(Set<String> lemmasStr, Site site) {
    
        int thresholdCountPages = (int) (searchService.getPageService().count() * 0.5);
    
        List<Lemma> list = new ArrayList<>();
        List<Lemma> lemmaList = searchService.getLemmaService()
                .getLemmasByLemmaAndSite(new ArrayList<>(lemmasStr), site);
        for (Lemma lemma : lemmaList) {
            if (lemma.getFrequency() < thresholdCountPages) {
                list.add(lemma);
            }
        }
        return list;
    }
    
    private List<Index> findIndexes() {
        
        List<Index> indexes = new ArrayList<>();
        
        for (Lemma lemma : lemmas) {
            List<Index> foundIndexes = searchService.getIndexService().findIndexesByLemmaId(lemma.getId());
            if (indexes.isEmpty()) {
                indexes.addAll(foundIndexes);
            } else {
                removeForNoneMatchPages(indexes, foundIndexes);
                addForIdenticalPages(indexes, foundIndexes);
            }
        }
        
        return indexes;
    }
    
    private void removeForNoneMatchPages(List<Index> indexes, List<Index> foundIndexes) {
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
                .toList();
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
        count = pagesAbsRelevance.size();
        
        return pagesAbsRelevance;
    }
    
    private Map<Page, Float> calculateRelRelevance(Map<Page, Float> pagesAbsRelevance) {
        
        Map<Page, Float> pagesRelRelevance = new HashMap<>();
        
        Float maxRelevance = pagesAbsRelevance
                .values()
                .stream()
                .max(Float::compareTo).orElse(null);
        
        for (Page page : pagesAbsRelevance.keySet()) {
            Float absRelevance = pagesAbsRelevance.get(page);
            Float relRelevance = absRelevance / maxRelevance;
            pagesRelRelevance.put(page, relRelevance);
        }
        
        return pagesRelRelevance;
    }
    
    private List<Finding> createFindings(Map<Page, Float> pagesRelRelevance, int offset, int limit) {
    
        return pagesRelRelevance.keySet().stream()
                .skip(offset)
                .map(page -> {
                    Finding finding = new Finding();
    
                    finding.setSite(page.getSite().getUrl());
                    finding.setSiteName(page.getSite().getName());
                    finding.setUri(page.getPath());
                    finding.setTitle(page.getDocument().title());
                    finding.setSnippet(createSnippet(page));
                    finding.setRelevance(pagesRelRelevance.get(page));
                    
                    return finding;
                })
                .sorted()
                .limit(limit)
                .toList();
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