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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Objects;

@NoArgsConstructor
@Entity
@Getter
@Setter
@ToString
public class Page implements Comparable<Page> {
    
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
    
//    @Column(name = "site_id", nullable = false)
//    private int siteId;
    
    
    public Page(String path) {
        this.path = path;
    }
    
    public Document getDocument() {
        return Jsoup.parse(content);
    }
    
    @Override
    public int compareTo(Page o) {
        return path.compareTo(o.path);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Page page = (Page) o;
        return id != 0 && Objects.equals(id, page.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
}
