package main.repository;

import main.model.Index;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndexRepository extends CrudRepository<Index, Integer> {
    
    @Query("SELECT i FROM index i WHERE i.page_id = :pageId")
    List<Index> findByPage(@Param("pageId") int pageId);
    
    @Query("SELECT i FROM index i WHERE i.lemma_id = :lemmaId")
    List<Index> findByLemma(@Param("lemmaId") int lemmaId);
    
}