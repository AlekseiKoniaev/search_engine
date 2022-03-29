package main.service;

import main.model.Lemma;
import main.model.Site;
import main.repository.LemmaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class LemmaService {
    
    private final LemmaRepository lemmaRepository;
    
    
    public LemmaService(LemmaRepository lemmaRepository) {
        this.lemmaRepository = lemmaRepository;
    }
    
    
    public void saveLemmas(List<Lemma> lemmas) {
        lemmaRepository.incrementInsertLemmas(lemmas);
    }
    
    public Lemma getLemmaById(int id) {
        return lemmaRepository.findById(id);
    }
    
    public Lemma getLemmaByLemmaAndSite(String lemma, Site site) {
        return site == null ?
                lemmaRepository.findByLemma(lemma) :
                lemmaRepository.findByLemmaAndSiteId(lemma, site.getId());
    }
    
    public List<Lemma> getLemmasByLemmaAndSite(List<String> lemmas, Site site) {
        return lemmas.stream().map(lemma -> getLemmaByLemmaAndSite(lemma, site))
                        .filter(Objects::nonNull).collect(Collectors.toList());
    }
    
    public int countBySiteId(int siteId) {
        return lemmaRepository.countBySiteId(siteId);
    }
    
    public void decrementAndUpdateLemma(Lemma lemma) {
        
        Lemma foundLemma = lemmaRepository.findByLemma(lemma.getLemma());
        
        if (foundLemma == null) {
            return;
        } else if (foundLemma.getFrequency() > 1) {
            lemmaRepository.decrementAndUpdateLemma(lemma.getLemma(), lemma.getSiteId());
        } else {
            lemmaRepository.deleteByLemmaAndSiteId(foundLemma.getLemma(), foundLemma.getSiteId());
        }
    }
    
    public void deleteBySiteId(int siteId) {
        lemmaRepository.deleteBySiteId(siteId);
    }
}
