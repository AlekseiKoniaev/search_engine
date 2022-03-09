package main.service;

import main.model.Lemma;
import main.model.Page;
import main.model.Site;

import java.util.List;

public interface LemmaService {
    
//    void saveLemma(Lemma lemma);
    void decrementAndUpdateLemma(Lemma lemma);
    void incrementAndInsertOrUpdateLemma(Lemma lemma);
    void saveLemmas(List<Lemma> lemmas);
    Lemma getLemmaById(int id);
    Lemma getLemmaByLemma(String lemma);
    Lemma getLemmaByLemmaAndSite(String lemma, Site site);
//    List<Lemma> getLemmasByLemma(List<String> lemmas);
    List<Lemma> getLemmasByLemmaAndSite(List<String> lemmas, Site site);
    int countForSite(Site site);
    void deleteBySite(Site site);
    
    void deleteLemma(Lemma lemma);
}
