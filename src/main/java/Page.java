import org.jsoup.nodes.Document;

import java.util.HashSet;
import java.util.Set;

public class Page implements Comparable<Page> {
    
    private String path;
    private int code;
    private Document content;
    private Set<Page> children;
    
    public Page(String path) {
        this.path = path;
        children = new HashSet<>();
    }
    
    public Page(String path, int code, Document content) {
        this.path = path;
        this.code = code;
        this.content = content;
    }
    
    public String getPath() {
        return path;
    }
    
    private void setPath(String path) {
        this.path = path;
    }
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public Document getContent() {
        return content;
    }
    
    public void setContent(Document content) {
        this.content = content;
    }
    
    public Set<Page> getChildren() {
        return children;
    }
    
    public void addChild(Page site) {
        children.add(site);
    }
    
    public void addChildren(Set<Page> children) {
        this.children.addAll(children);
    }
    
    @Override
    public int compareTo(Page o) {
        return path.compareTo(o.path);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Page site = (Page) o;
    
        return path.equals(site.path);
    }
    
    @Override
    public int hashCode() {
        return path.hashCode();
    }
    
}
