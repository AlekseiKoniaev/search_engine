package main.api.response.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Finding implements Comparable<Finding> {
    
    private String uri;
    private String title;
    private String snippet;
    private float relevance;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Finding finding = (Finding) o;
    
        return uri.equals(finding.uri);
    }
    
    @Override
    public int hashCode() {
        return uri.hashCode();
    }
    
    @Override
    public int compareTo(Finding o) {
        return relevance != o.relevance ? Float.compare(o.relevance, relevance) : uri.compareTo(o.uri);
    }
    
    @Override
    public String toString() {
        return uri + "\t\t" + relevance + "\n\t" + title + "\n" + snippet;
    }
}
