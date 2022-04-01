package main.indexer;

import main.lemmatizer.Lemmatizer;
import main.model.Field;
import main.model.Index;
import main.model.Lemma;
import main.model.Page;
import main.model.Site;
import main.service.FieldService;
import main.service.IndexService;
import main.service.LemmaService;
import main.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class PageIndexer {
    
    private final FieldService fieldService;
    private final PageService pageService;
    private final LemmaService lemmaService;
    private final IndexService indexService;
    
    private Page page;
    private Site site;
    private List<Field> fields;
    private Map<String, Map<String, Integer>> lemmas;
    private List<Index> indexes;
    
    
    @Autowired
    public PageIndexer(FieldService fieldService,
                       PageService pageService,
                       LemmaService lemmaService,
                       IndexService indexService) {
        this.fieldService = fieldService;
        this.pageService = pageService;
        this.lemmaService = lemmaService;
        this.indexService = indexService;
    }
    
    public void init(Page page, Site site) {
        this.page = page;
        this.site = site;
    }
    
    public void index() {
        fields = fieldService.getAllFields();
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
                    lemma.setSiteId(page.getSiteId());
                    return lemma;
                })
                .collect(Collectors.toList());
        
        lemmaService.saveLemmas(uniqueLemmas);
    }
    
    
    private List<Index> createIndexes() {
        Map<String, Float> rankForLemmas = calculateRankForLemmas();
        List<Index> indexes = new ArrayList<>();
        Page page = pageService.getPageByPathAndSiteId(
                this.page.getPath(), site.getId());
        List<Lemma> foundLemmas = lemmaService.getLemmasByLemmasAndSite(
                new ArrayList<>(rankForLemmas.keySet()), site);
        
        for (Lemma lemma : foundLemmas) {
            
            float rank = rankForLemmas.get(lemma.getLemma());
            
            Index index = new Index();
            index.setPageId(page.getId());
            index.setLemmaId(lemma.getId());
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
    
    
    private void addIndexesToDB() {
        indexService.saveIndexes(indexes);
    }
}
