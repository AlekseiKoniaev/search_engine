package main.model;

import org.jsoup.nodes.Document;

import java.util.Objects;

public class Page implements Comparable<Page> {
    
    private int id;
    private String path;
    private int code;
    private Document document;
    
    public Page() {}
    
    public Page(String path) {
        this.path = path;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getPath() {
        return path;
    }
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public Document getDocument() {
        return document;
    }
    
    public void setDocument(Document document) {
        this.document = document;
    }
    
    public String getContent() {
        return Objects.requireNonNullElse(document, "").toString();
    }
    
    @Override
    public int compareTo(Page o) {
        return path.compareTo(o.path);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Page page = (Page) o;
    
        return Objects.equals(path, page.path);
    }
    
    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }
}
