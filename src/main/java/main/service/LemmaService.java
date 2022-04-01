package main.service;

import main.model.Lemma;
import main.model.Site;
import main.repository.LemmaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
    
    public List<Lemma> getLemmasByLemmasAndSite(List<String> lemmas, Site site) {
        return site == null ?
                lemmaRepository.findByLemmas(lemmas) :
                lemmaRepository.findByLemmasAndSiteId(lemmas, site.getId());
    }
    
    public int countBySiteId(int siteId) {
        return lemmaRepository.countBySiteId(siteId);
    }
    
    public void decrementAndUpdateLemma(Lemma lemma) {
        
        Lemma foundLemma = lemmaRepository.findByLemmaAndSiteId(lemma.getLemma(), lemma.getSiteId());
        
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
