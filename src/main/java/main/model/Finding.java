package main.model;

public class Finding implements Comparable<Finding> {
    
    private String uri;
    private String title;
    private String snippet;
    private float relevance;
    
    public String getUri() {
        return uri;
    }
    
    public void setUri(String uri) {
        this.uri = uri;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getSnippet() {
        return snippet;
    }
    
    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }
    
    public float getRelevance() {
        return relevance;
    }
    
    public void setRelevance(float relevance) {
        this.relevance = relevance;
    }
    
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
