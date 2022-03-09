package main.repository;

import main.model.Lemma;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface LemmaRepository extends CrudRepository<Lemma, Integer> {
    
    @Query("SELECT l FROM Lemma l WHERE l.lemma = :lemma")
    Lemma findByLemma(@Param("lemma") String lemma);
    
    @Query("SELECT l FROM Lemma l WHERE l.lemma = :lemma and l.site.id = :siteId")
    Lemma findByLemmaAndSite(@Param("lemma") String lemma, @Param("siteId") int siteId);
    
    @Query("SELECT count(l) FROM Lemma l WHERE l.site.id = :siteId")
    int countForSite(@Param("siteId") int siteId);
    
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO lemma(lemma, frequency, site_id) " +
            "VALUES (:lemma, 1, :siteId) " +
            "ON DUPLICATE KEY UPDATE frequency = frequency + 1",
            nativeQuery = true)
    void incrementInsertLemma(@Param("lemma") String lemma, @Param("siteId") int siteId);
    
    @Transactional
    @Modifying
    @Query("UPDATE Lemma l SET l.frequency = l.frequency - 1 WHERE l.lemma = :lemma")
    void decrementAndUpdateLemma(String lemma);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM Lemma l WHERE l.site.id = :siteId")
    void deleteBySite(@Param("siteId") int siteId);
    
}
