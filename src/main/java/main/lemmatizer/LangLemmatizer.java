package main.lemmatizer;

import org.apache.lucene.morphology.LuceneMorphology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        
        getLemmas().forEach(normalWord -> {
                    int count = wordCount.getOrDefault(normalWord, 0);
                    wordCount.put(normalWord, count + 1);
                });
        return wordCount;
    }
    
    public Map<String, List<String>> getInitialForms() {
        Map<String, List<String>> initialForms = new HashMap<>();
        for (String initialWord : getWords(text)) {
            String lemma = getNormalForm(initialWord);
            if (initialForms.containsKey(lemma)) {
                initialForms.get(lemma).add(initialWord);
            } else {
                List<String> initialWords = new ArrayList<>();
                initialWords.add(initialWord);
                initialForms.put(lemma, initialWords);
            }
        }
        return initialForms;
    }
    
    private Set<String> getLemmas() {
        return getWords(text).stream()
                .map(this::getNormalForm)
                .collect(Collectors.toSet());
    }
    
    private List<String> getWords(String text) {
        return splitText(clearText(text)).stream()
                .filter(this::isAllowablePartOfSpeech)
                .collect(Collectors.toList());
    }
    
    private List<String> splitText(String text) {
        return text.isEmpty() ? new ArrayList<>() : List.of(text.split(" "));
    }
    
    private boolean isAllowablePartOfSpeech(String word) {
        List<String> morphInfoList;
        synchronized (LangLemmatizer.class) {
            morphInfoList = getLuceneMorphology().getMorphInfo(word);
        }
        return morphInfoList.stream()
                .map(this::getPartOfSpeech)
                .noneMatch(currentPartOfSpeech -> Arrays.stream(getExcludedPartOfSpeech())
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
