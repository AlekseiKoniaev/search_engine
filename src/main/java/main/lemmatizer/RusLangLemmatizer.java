package main.lemmatizer;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;

public class RusLangLemmatizer extends LangLemmatizer {
    
    private static LuceneMorphology luceneMorphology;
    private static final String[] excludedPartOfSpeech = new String[]{
            "МС", "ПРЕДЛ", "ПОСЛ", "СОЮЗ", "МЕЖД", "ЧАСТ", "ВВОДН", "ФРАЗ"};
    
    public RusLangLemmatizer(String text) {
        super(text);
    }
    
    @Override
    void init() {
        synchronized (RusLangLemmatizer.class) {
            if (luceneMorphology == null) {
                try {
                    luceneMorphology = new RussianLuceneMorphology();
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
                .replaceAll("[^а-я ]", " ")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }
    
}
