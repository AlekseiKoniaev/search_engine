package repository.DBStructure;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "_page")
public class Page {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "_id", nullable = false)
    private int id;
    
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "_path", nullable = false, unique = true)
    private String path;
    
    @Column(name = "_code", nullable = false)
    private int code;
    
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "_content", nullable = false)
    private String content;
    
    public Page() {
    }
    
    public Page(String path) {
        this.path = path;
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
