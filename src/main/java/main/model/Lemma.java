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
    
//    @Column(nullable = false)
//    private int siteId;
    
    
    public void incrementFrequency() {
        ++frequency;
    }
    
    @Override
    public int compareTo(Lemma o) {
        return frequency != o.frequency ?
                Integer.compare(frequency, o.frequency) : lemma.compareTo(o.lemma);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Lemma lemma = (Lemma) o;
        return id != 0 && Objects.equals(id, lemma.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
