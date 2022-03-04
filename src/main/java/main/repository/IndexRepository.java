package main.repository;

import main.model.Index;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface IndexRepository extends CrudRepository<Index, Integer> {
    
    @Query("SELECT i FROM Index i WHERE i.page.id = :pageId")
    List<Index> findByPage(@Param("pageId") int pageId);
    
    @Query("SELECT i FROM Index i WHERE i.lemma.id = :lemmaId")
    List<Index> findByLemma(@Param("lemmaId") int lemmaId);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM Index i WHERE i.page.id = :pageId")
    void deleteByPageId(@Param("pageId") int pageId);
}
