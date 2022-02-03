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
@Table(name = "_lemma")
public class Lemma {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "_id", nullable = false)
    private int id;
    
    @Column(name = "_lemma", nullable = false, unique = true, length = 240)
    private String lemma;
    
    @Column(name = "_frequency", nullable = false)
    private int frequency;
    
}
