package main.lemmatizer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lemmatizer {
    
    private final String text;
    
    public Lemmatizer(String text) {
        this.text = text;
    }
    
    public Map<String, Integer> getLemmas() {
        Map<String, Integer> lemmas = new HashMap<>();
        lemmas.putAll(new RusLangLemmatizer(text).getCountLemmas());
        lemmas.putAll(new EngLangLemmatizer(text).getCountLemmas());
        return lemmas;
    }
    
    public Map<String, List<String>> getInitialForms() {
        Map<String, List<String>> initialForms = new HashMap<>();
        initialForms.putAll(new RusLangLemmatizer(text).getInitialForms());
        initialForms.putAll(new EngLangLemmatizer(text).getInitialForms());
        return initialForms;
    }
}
