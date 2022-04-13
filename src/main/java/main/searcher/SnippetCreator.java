package main.searcher;

import main.lemmatizer.Lemmatizer;
import main.model.Field;
import main.model.Lemma;
import main.model.Page;
import main.service.FieldService;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SnippetCreator {
    
    private final List<Field> fields;
    private final Page page;
    private final List<Lemma> lemmas;
    
    
    public SnippetCreator(List<Field> fields, Page page, List<Lemma> lemmas) {
        this.fields = fields;
        this.page = page;
        this.lemmas = lemmas;
    }
    
    public String getSnippet() {
        return createSnippet();
    }
    
    private String createSnippet() {
        
        StringBuilder snippet = new StringBuilder();
        Document document = page.getDocument();
        
        for (Field field : fields) {
            String selector = field.getSelector();
            String textByField = document.select(selector).text();
            String fragment = formatFragment(textByField);
            snippet.append(fragment).append("\t\n");
        }
        
        return snippet.toString();
    }
    
    private String formatFragment(String text) {
        
        StringBuilder fragment = new StringBuilder(text);
        Map<Integer, Integer> matchIndexMap = getMatchIndexMap(text);
        
        int count = 0;
        for (Integer begin : matchIndexMap.keySet()) {
            if (count > 10) {
                break;
            }
            Integer end = matchIndexMap.get(begin);
            fragment.insert(end, "</b>");
            fragment.insert(begin, "<b>");
            count++;
        }
        
        optimiseLength(fragment);
        
        return fragment.toString();
    }
    
    private Map<Integer, Integer> getMatchIndexMap(String text) {
    
        Map<Integer, Integer> matchIndexMap = new TreeMap<>(Comparator.reverseOrder());
        text = text.toLowerCase();
        Map<String, List<String>> initialForms = new Lemmatizer(text).getInitialForms();
        
        for (Lemma lemma : lemmas) {
            
            List<String> initialWords = initialForms.get(lemma.getLemma());
            if (initialWords == null) {
                continue;
            }
            
            for (String initialWord : initialWords) {
                findMatchIndexes(text, matchIndexMap, initialWord);
            }
        }
        
        return matchIndexMap;
    }
    
    private void findMatchIndexes(String text, Map<Integer, Integer> matchIndexMap, String initialWord) {
    
        int begin;
        int end = 0;
        while (true) {
            begin = findBeginIndex(initialWord, end, text);
            if (begin == -1) {
                break;
            }
            
            end = findEndIndex(text, begin);
    
            matchIndexMap.put(begin, end);
        }
    }
    
    private int findBeginIndex(String word, int end, String text) {
        int begin;
        while (true) {
            begin = text.indexOf(word, end);
            if (begin <= 0) {
                return begin;
            }
            char preBeginChar = text.charAt(begin - 1);
            if (isLetterSymbol(preBeginChar)) {
                end = begin + word.length();
            } else {
                return begin;
            }
        }
    }
    
    
    private int findEndIndex(String text, int begin) {
        int end = begin;
        char endSymbol;
        while (end < text.length()) {
            endSymbol = text.charAt(end);
            if (!isLetterSymbol(endSymbol)) {
                break;
            }
            end++;
        }
        return end;
    }
    
    private boolean isLetterSymbol(char symbol) {
        return String.valueOf(symbol).matches("[a-zа-я]");
    }
    
    private void optimiseLength(StringBuilder fragment) {
        
        Map<Integer, Integer> tagIndexMap = getTagIndexMap(fragment);
        int finish = fragment.length();
        
        for (Integer begin : tagIndexMap.keySet()) {
            int end = tagIndexMap.get(begin) + 4;
            int diff = finish - end;
            if (diff > 150) {
                trimFragment(fragment, end, finish);
            }
            finish = begin;
        }
        if (finish > 100) {
            trimFragment(fragment, 0, finish);
        }
    }
    
    private Map<Integer, Integer> getTagIndexMap(StringBuilder fragment) {
        
        Map<Integer, Integer> tagIndexMap = new TreeMap<>(Comparator.reverseOrder());
        int begin;
        int end = 0;
        
        for (;;) {
            begin = fragment.indexOf("<b>", end);
            if (begin == -1) {
                break;
            }
            end = fragment.indexOf("</b>", begin);
            tagIndexMap.put(begin, end);
        }
        
        return tagIndexMap;
    }
    
    private void trimFragment(StringBuilder fragment, int begin, int end) {
        
        int beginDel;
        int endDel;
        
        if (end == fragment.length()) {
            beginDel = fragment.indexOf(" ", begin + 50);
            endDel = end;
        } else if (begin == 0) {
            beginDel = 0;
            endDel = fragment.indexOf(" ", end - 60);
        } else {
            beginDel = fragment.indexOf(" ", begin + 50);
            endDel = fragment.indexOf(" ", end - 60);
        }
        
        fragment.replace(beginDel, endDel, "...");
    }
}
