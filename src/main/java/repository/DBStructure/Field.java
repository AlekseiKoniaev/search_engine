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
@Table(name = "_field")
public class Field {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "_id", nullable = false)
    private int id;
    
    @Column(name = "_name", nullable = false)
    private String name;
    
    @Column(name = "_selector", nullable = false)
    private String selector;
    
    @Column(name = "_weight", nullable = false)
    private float weight;
    
}
