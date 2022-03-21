package main.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@NoArgsConstructor
@Getter
@Setter
public class Page {
    
    private int id;
    private String path;
    private int code;
    private String content;
    private Integer siteId;
    
    
    public Page(String path) {
        this.path = path;
    }
    
    public Document getDocument() {
        return Jsoup.parse(content);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Page page = (Page) o;
        
        if (!path.equals(page.path)) return false;
        return siteId.equals(page.siteId);
    }
    
    @Override
    public int hashCode() {
        int result = path.hashCode();
        result = 31 * result + siteId.hashCode();
        return result;
    }
}
