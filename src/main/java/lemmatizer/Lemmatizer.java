package lemmatizer;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lemmatizer {
    
    private static LuceneMorphology luceneMorphology;
    private static final String[] excludedPartOfSpeech = new String[]{
            "МС", "ПРЕДЛ", "ПОСЛ", "СОЮЗ", "МЕЖД", "ЧАСТ", "ВВОДН", "ФРАЗ"};
    
    private final String text;
    
    public Lemmatizer(String text) {
        init();
        this.text = text;
    }
    
    public static void init() {
        synchronized (Lemmatizer.class) {
            if (luceneMorphology == null) {
                try {
                    luceneMorphology = new RussianLuceneMorphology();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public Map<String, Integer> countLemmas() {
        Map<String, Integer> wordCount = new HashMap<>();
        String clearText = clearText(text);
        List<String> words = splitText(clearText);
        words.stream().filter(word -> !isExcludedPartOfSpeech(word))
                .map(this::getNormalForm)
                .forEach(normalWord -> {
                    int count = wordCount.getOrDefault(normalWord, 0);
                    wordCount.put(normalWord, count + 1);
                });
        return wordCount;
    }
    
    private String clearText(String text) {
        return text.toLowerCase()
                .replaceAll("[^а-я ]", " ")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }
    
    private List<String> splitText(String text) {
        return List.of(text.split(" "));
    }
    
    private boolean isExcludedPartOfSpeech(String word) {
        List<String> wordBaseForms;
        synchronized (Lemmatizer.class) {
            wordBaseForms = luceneMorphology.getMorphInfo(word);
        }
        for (String wordBaseForm : wordBaseForms) {
            String currentPartOfSpeech = wordBaseForm.split(" ")[1];
            for (String partOfSpeech : excludedPartOfSpeech) {
                if (currentPartOfSpeech.matches(partOfSpeech)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private String getNormalForm(String word) {
        synchronized (Lemmatizer.class) {
            return luceneMorphology.getNormalForms(word).get(0);
        }
    }

}
