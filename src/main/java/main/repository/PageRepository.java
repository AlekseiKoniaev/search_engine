package main.repository;

import main.model.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PageRepository extends CrudRepository<Page, Integer> {
    
    @Query("SELECT p FROM page p WHERE p.path = :path")
    Page getPageByPath(@Param("path") String path);
    
}
