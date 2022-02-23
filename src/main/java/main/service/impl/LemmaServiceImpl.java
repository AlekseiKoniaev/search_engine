package main.service.impl;

import main.model.Lemma;
import main.repository.LemmaRepository;
import main.service.LemmaService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class LemmaServiceImpl implements LemmaService {
    
    @Autowired
    private LemmaRepository lemmaRepository;
    
    
    @Override
    public void insertLemma(Lemma lemma) {
        Lemma foundLemma = getLemmaById(lemma.getId());
        if (foundLemma == null) {
            lemma.setFrequency(1);
        } else {
            lemma = foundLemma;
            foundLemma.incrementFrequency();
        }
        lemmaRepository.save(lemma);
    }
    
    @Override
    public void insertLemmas(List<Lemma> lemmas) {
        lemmas.forEach(this::insertLemma);
    }
    
    @Override
    public Lemma getLemmaById(int id) {
        return lemmaRepository.findById(id).orElse(null);
    }
    
    @Override
    public Lemma getLemmaByLemma(String lemma) {
        return lemmaRepository.findByLemma(lemma);
    }
}
