package main.service.impl;

import main.model.Lemma;
import main.model.Page;
import main.model.Site;
import main.repository.LemmaRepository;
import main.service.LemmaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class LemmaServiceImpl implements LemmaService {
    
    @Autowired
    private LemmaRepository lemmaRepository;
    
    
    @Override
    @Transactional
    public void decrementAndUpdateLemma(Lemma lemma) {
        
        Lemma foundLemma = lemmaRepository.findByLemma(lemma.getLemma());
        
        if (foundLemma == null) {
            return;
        } else if (foundLemma.getFrequency() > 1) {
            lemmaRepository.decrementAndUpdateLemma(lemma.getLemma());
        } else {
            lemmaRepository.delete(foundLemma);
        }
    }
    
    @Override
    public void incrementAndInsertOrUpdateLemma(Lemma lemma) {
        lemmaRepository.incrementInsertLemma(lemma.getLemma(), lemma.getSite().getId());
    }
    
    @Override
    public void saveLemmas(List<Lemma> lemmas) {
        lemmas.forEach(this::incrementAndInsertOrUpdateLemma);
    }
    
    @Override
    public Lemma getLemmaById(int id) {
        return lemmaRepository.findById(id).orElse(null);
    }
    
    @Override
    public Lemma getLemmaByLemma(String lemma) {
        return lemmaRepository.findByLemma(lemma);
    }
    
    @Override
    public Lemma getLemmaByLemmaAndSite(String lemma, Site site) {
        return site == null ? getLemmaByLemma(lemma) :
                lemmaRepository.findByLemmaAndSite(lemma, site.getId());
    }
    
    @Override
    public List<Lemma> getLemmasByLemmaAndSite(List<String> lemmas, Site site) {
        return lemmas.stream().map(lemma -> getLemmaByLemmaAndSite(lemma, site))
                        .filter(Objects::nonNull).toList();
    }
    
    @Override
    public int countForSite(Site site) {
        return lemmaRepository.countForSite(site.getId());
    }
    
    @Override
    public void deleteLemma(Lemma lemma) {
        lemmaRepository.deleteById(lemma.getId());
    }
    
    @Override
    @Transactional
    public void deleteBySite(Site site) {
        lemmaRepository.deleteBySite(site.getId());
    }
}
