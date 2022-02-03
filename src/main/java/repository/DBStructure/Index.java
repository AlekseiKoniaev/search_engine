package repository.DBStructure;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "_index")
public class Index {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "_id", nullable = false)
    private int id;
    
    @Column(name = "_page_id", nullable = false)
    private int page_id;
    
    @Column(name = "_lemma_id", nullable = false)
    private int lemma_id;
    
    @Column(name = "_rank", nullable = false)
    private float rank;
    
}
