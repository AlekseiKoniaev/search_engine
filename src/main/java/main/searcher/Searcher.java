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
import main.service.FieldService;
import main.service.IndexService;
import main.service.LemmaService;
import main.service.PageService;
import main.service.SiteService;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class Searcher {
    
    private static final float THRESHOLD = 0.5f;
    
    private final FieldService fieldService;
    private final PageService pageService;
    private final LemmaService lemmaService;
    private final IndexService indexService;
    private final SiteService siteService;
    
    private String query;
    private Site site;
    private Map<Page, Float> pagesRelRelevance;
    private List<Field> fields;
    private List<Lemma> lemmas;
    private List<Index> indexes;
    private List<Page> pages;
    
    @Getter
    private SearchStatus status;
    @Getter
    private int count;
    @Getter
    private List<Finding> searchResult;
    
    
    @Autowired
    public Searcher(FieldService fieldService,
                    PageService pageService,
                    LemmaService lemmaService,
                    IndexService indexService,
                    SiteService siteService) {
        this.fieldService = fieldService;
        this.pageService = pageService;
        this.lemmaService = lemmaService;
        this.indexService = indexService;
        this.siteService = siteService;
    }
    
    
    public void search(String query, String siteUrl, int offset, int limit) {
        
        status = SearchStatus.READY;
        
        boolean isNewQuery = this.query == null || !this.query.equals(query);
        boolean isOtherSiteUrl = (site != null && !site.getUrl().equals(siteUrl)) ||
                (site == null && !siteUrl.equals(""));
        boolean isNewSearch = isNewQuery || isOtherSiteUrl;
        
        if (isNewSearch) {
            
            this.query = query;
            fields = fieldService.getAllFields();
            Lemmatizer lemmatizer = new Lemmatizer(query);
            Set<String> lemmasStr = lemmatizer.getLemmas().keySet();
    
            List<Site> sites;
            site = null;
            if (siteUrl.equals("")) {
                sites = siteService.getAllSites();
            } else {
                sites = new ArrayList<>();
                site = siteService.getSiteByUrl(siteUrl);
                sites.add(site);
            }
    
            lemmas = getLemmas(lemmasStr, site);
            if (lemmas.isEmpty()) {
                status = SearchStatus.WRONG_QUERY;
                return;
            }
    
            indexes = new ArrayList<>();
            sites.forEach(s -> indexes.addAll(findIndexes(s)));
            if (indexes.isEmpty()) {
                status = SearchStatus.NOT_FOUND;
                return;
            }
    
            pages = getPages();
    
            Map<Page, Float> pagesAbsRelevance = calculateAbsRelevance();
            pagesRelRelevance = calculateRelRelevance(pagesAbsRelevance);
        }
    
        searchResult = createFindings(pagesRelRelevance, offset, limit);
        
        status = SearchStatus.OK;
    }
    
    
    private List<Lemma> getLemmas(Set<String> lemmasStr, Site site) {
    
        int thresholdCountPages = (int) (pageService.countBySite(site) * THRESHOLD);
    
        List<Lemma> list = new ArrayList<>();
        List<Lemma> lemmaList = lemmaService.getLemmasByLemmasAndSite(new ArrayList<>(lemmasStr), site);
        for (Lemma lemma : lemmaList) {
            if (lemma.getFrequency() < thresholdCountPages) {
                list.add(lemma);
            }
        }
        return list;
    }
    
    
    private List<Index> findIndexes(Site site) {
        Map<Integer, List<Index>> groupedIndexes = new HashMap<>();
        
        for (Lemma lemma : lemmas) {
            if (site.getId() != lemma.getSiteId()) {
                continue;
            }
            List<Index> foundIndexes = indexService.findIndexesByLemmaId(lemma.getId());
            Map<Integer, List<Index>> groupedFoundIndexes = foundIndexes.stream()
                    .collect(Collectors.groupingBy(Index::getPageId));
            if (groupedIndexes.isEmpty()) {
                groupedIndexes.putAll(groupedFoundIndexes);
            } else {
                mergeIndexes(groupedIndexes,groupedFoundIndexes);
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
    
    private List<Page> getPages() {
        return indexes.stream()
                .map(index -> pageService.getPageById(index.getPageId()))
                .distinct()
                .collect(Collectors.toList());
    }
    
    private Map<Page, Float> calculateAbsRelevance() {
        
        Map<Page, Float> pagesAbsRelevance = new HashMap<>();
        
        for (Page page : pages) {
            
            int pageId = page.getId();
            float absRelevance = 0.0f;
            for (Index index : indexes) {
                if (pageId == index.getPageId()) {
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
                    
                    Site site = siteService.getSiteById(page.getSiteId());
                    finding.setSite(site.getUrl());
                    finding.setSiteName(site.getName());
                    finding.setUri(page.getPath());
                    finding.setTitle(page.getDocument().title());
                    finding.setSnippet(createSnippet(page));
                    finding.setRelevance(pagesRelRelevance.get(page));
                    
                    return finding;
                })
                .sorted()
                .limit(limit)
                .collect(Collectors.toList());
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
        
        int count = 0;
        for (Integer begin : matchIndexMap.keySet()) {
            if (count > 10) {
                break;
            }
            Integer end = matchIndexMap.get(begin);
            fragment.insert(end, "</b>");
            fragment.insert(begin, "<b>");
            count++;
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