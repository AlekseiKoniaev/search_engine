package main.service;

import main.model.Lemma;

import java.util.List;

public interface LemmaService {
    
    void insertLemma(Lemma lemma);
    void insertLemmas(List<Lemma> lemmas);
    Lemma getLemmaById(int id);
    Lemma getLemmaByLemma(String lemma);
    
}
