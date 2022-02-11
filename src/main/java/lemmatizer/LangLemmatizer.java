package lemmatizer;

import org.apache.lucene.morphology.LuceneMorphology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class LangLemmatizer {
    
    private final String text;
    
    abstract void init();
    abstract LuceneMorphology getLuceneMorphology();
    abstract String[] getExcludedPartOfSpeech();
    abstract String clearText(String text);
    
    
    public LangLemmatizer(String text) {
        this.text = text;
    }

    
    public Map<String, Integer> getCountLemmas() {
        Map<String, Integer> wordCount = new HashMap<>();
        
        getWords(text).stream()
                .filter(word -> !isExcludedPartOfSpeech(word))
                .map(this::getNormalForm)
                .forEach(normalWord -> {
                    int count = wordCount.getOrDefault(normalWord, 0);
                    wordCount.put(normalWord, count + 1);
                });
        return wordCount;
    }
    
    private List<String> getWords(String text) {
        return splitText(clearText(text));
    }
    
    private List<String> splitText(String text) {
        return text.isEmpty() ? new ArrayList<>() : List.of(text.split(" "));
    }
    
    private boolean isExcludedPartOfSpeech(String word) {
        List<String> morphInfoList;
        synchronized (LangLemmatizer.class) {
            morphInfoList = getLuceneMorphology().getMorphInfo(word);
        }
        return morphInfoList.stream()
                .map(this::getPartOfSpeech)
                .anyMatch(currentPartOfSpeech -> Arrays.stream(getExcludedPartOfSpeech())
                        .anyMatch(currentPartOfSpeech::matches));
    }
    
    private String getPartOfSpeech(String morphInfo) {
        return morphInfo.split(" ")[1];
    }
    
    private String getNormalForm(String word) {
        synchronized (LangLemmatizer.class) {
            return getLuceneMorphology().getNormalForms(word).get(0);
        }
    }
}
