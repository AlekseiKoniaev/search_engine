package main.repository;

import main.model.Lemma;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LemmaRepository extends CrudRepository<Lemma, Integer> {
    
    @Query("SELECT l FROM lemma l WHERE l.lemma = :lemma")
    Lemma findByLemma(@Param("lemma") String lemma);
    
}
