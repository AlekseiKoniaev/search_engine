package main.lemmatizer;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;

import java.io.IOException;

public class EngLangLemmatizer extends LangLemmatizer {
    
    private static LuceneMorphology luceneMorphology;
    private static final String[] excludedPartOfSpeech = new String[]{
            "CONJ", "INT", "PREP", "PART", "ARTICLE"};
    
    public EngLangLemmatizer(String text) {
        super(text);
    }
    
    @Override
    void init() {
        synchronized (EngLangLemmatizer.class) {
            if (luceneMorphology == null) {
                try {
                    luceneMorphology = new EnglishLuceneMorphology();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    @Override
    LuceneMorphology getLuceneMorphology() {
        if (luceneMorphology == null) {
            init();
        }
        return luceneMorphology;
    }
    
    @Override
    String[] getExcludedPartOfSpeech() {
        return excludedPartOfSpeech;
    }
    
    @Override
    String clearText(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-z ]", " ")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }
    
}
