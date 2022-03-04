package main.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Objects;

@NoArgsConstructor
@Entity
@Getter
@Setter
@ToString
public class Page {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;
    
    @Column(nullable = false, unique = true, length = 333)
    private String path;
    
    @Column(nullable = false)
    private int code;
    
    @Column(nullable = false)
    @Type(type = "org.hibernate.type.TextType")
    private String content;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;
    
    
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
        return site.equals(page.site);
    }
    
    @Override
    public int hashCode() {
        int result = path.hashCode();
        result = 31 * result + site.hashCode();
        return result;
    }
}
