package main.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Objects;

@NoArgsConstructor
@Entity
@Getter
@Setter
@ToString
public class Lemma implements Comparable<Lemma> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;
    
    @Column(nullable = false, unique = true)
    private String lemma;
    
    @Column(nullable = false)
    private int frequency;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;
    
    
    public void incrementFrequency() {
        ++frequency;
    }
    
    public void decrementFrequency() {
        --frequency;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Lemma lemma1 = (Lemma) o;
        
        if (!lemma.equals(lemma1.lemma)) return false;
        return site.equals(lemma1.site);
    }
    
    @Override
    public int hashCode() {
        int result = lemma.hashCode();
        result = 31 * result + site.hashCode();
        return result;
    }
    
    @Override
    public int compareTo(Lemma o) {
        int diff = site.getId() - o.site.getId();
        if (diff != 0) {
            return diff;
        } else {
            return frequency != o.frequency ?
                    Integer.compare(frequency, o.frequency) : lemma.compareTo(o.lemma);
        }
    }
}
