package main.repository;

import main.model.Page;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PageRepository extends CrudRepository<Page, Integer> {
    
    @Query("SELECT p FROM Page p WHERE p.path = :path")
    Page findByPath(@Param("path") String path);
    
    @Query("SELECT p FROM Page p WHERE p.site.id = :siteId and p.path = :path")
    Page findBySiteIdAndPath(@Param("siteId") int siteId, @Param("path") String path);
    
    @Query("SELECT p FROM Page p WHERE p.site.id = :siteId")
    List<Page> findBySiteId(@Param("siteId") int siteId);
    
    @Query("SELECT count(p) FROM Page p WHERE p.site.id = :siteId")
    int countForSite(@Param("siteId") int siteId);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM Page p WHERE p.site.id = :siteId")
    void deleteBySiteId(@Param("siteId") int siteId);       // todo : editing
    
    @Transactional
    @Modifying
    @Query("DELETE FROM Page p WHERE p.site.id = :siteId and p.path = :path")
    void deleteBySiteIdAndPath(@Param("siteId") int siteId, @Param("path") String path);
    
}
