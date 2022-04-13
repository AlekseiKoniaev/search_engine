package main.api.response.model;

import lombok.Getter;
import lombok.Setter;
import main.model.Index;
import main.model.Lemma;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FoundPageObject implements Comparable<FoundPageObject> {
    
    private int pageId;
    private final List<Lemma> lemmas = new ArrayList<>();
    private final List<Index> indexes = new ArrayList<>();
    private float relevance;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        FoundPageObject that = (FoundPageObject) o;
    
        return pageId == that.pageId;
    }
    
    @Override
    public int hashCode() {
        return pageId;
    }
    
    @Override
    public int compareTo(FoundPageObject o) {
        return Float.compare(relevance, o.getRelevance());
    }
}
