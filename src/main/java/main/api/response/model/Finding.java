package main.api.response.model;

import lombok.Getter;
import lombok.Setter;
import main.model.Page;

@Getter
@Setter
public class Finding implements Comparable<Finding> {
    
    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private float relevance;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Finding finding = (Finding) o;
        
        if (!site.equals(finding.site)) return false;
        return uri.equals(finding.uri);
    }
    
    @Override
    public int hashCode() {
        int result = site.hashCode();
        result = 31 * result + uri.hashCode();
        return result;
    }
    
    @Override
    public int compareTo(Finding o) {
        return relevance != o.relevance ? Float.compare(o.relevance, relevance) :
                !site.equals(o.site) ? site.compareTo(o.site) : uri.compareTo(o.uri);
    }
    
    @Override
    public String toString() {
        return uri + "\t\t" + relevance + "\n\t" + title;
    }
}
