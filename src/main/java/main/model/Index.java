package main.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "_index")
@Getter
@Setter
public class Index {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "page_id", nullable = false, foreignKey = @ForeignKey(name = "PAGE_ID_FK"))
    private Page page;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "lemma_id", nullable = false, foreignKey = @ForeignKey(name = "LEMMA_ID_FK"))
    private Lemma lemma;
    
    @Column(name = "_rank", nullable = false)
    private float rank;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Index index = (Index) o;
        return id != 0 && Objects.equals(id, index.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
