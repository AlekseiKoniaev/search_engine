package main.repository;

import main.model.Site;
import main.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteRepository extends JpaRepository<Site, Integer> {
    
    @Query("SELECT s FROM Site s WHERE s.url = :url")
    Site findByUrl(@Param("url") String url);
    
//    @Modifying
//    @Query("UPDATE Site s SET s.status = :status")
//    void updateSiteStatus(String status);
    
}
