package main.repository;

import main.model.Site;
import main.model.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Repository
public class SiteRepository {
    
    private final RowMapper<Site> rowMapper = (rs, rowNum) -> {
        Site site = new Site();
        site.setId(rs.getInt("id"));
        site.setStatus(Status.valueOf(rs.getString("status")));
        site.setStatusTime(rs.getTimestamp("status_time").toLocalDateTime());
        site.setLastError(rs.getString("last_error"));
        site.setUrl(rs.getString("url"));
        site.setName(rs.getString("name"));
        return site;
    };
    
    private final JdbcTemplate jdbcTemplate;
    
    
    @Autowired
    public SiteRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    
    public void save(Site site) {
        
        String status = site.getStatus().toString();
        LocalDateTime statusTime = site.getStatusTime();
        String sql = "insert into site(status, status_time, last_error, url, name) " +
                "values (?, ?, ?, ?, ?) on duplicate key update status = ?, status_time = ?";
        
        jdbcTemplate.update(sql, status, statusTime, site.getLastError(),
                site.getUrl(), site.getName(), status, statusTime);
    }
    
    public void updateStatus(Site site) {
        jdbcTemplate.update("update site set status = ?, status_time = ? where url = ?",
                site.getStatus().toString(), site.getStatusTime(), site.getUrl());
    }
    
    public void updateStatusTime(Site site) {
        jdbcTemplate.update("update site set status_time = ? where url = ?",
                LocalDateTime.now(), site.getUrl());
    }
    
    public Site findById(int id) {
        try {
            return jdbcTemplate.queryForObject("select * from site where id = ?", rowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    
    public Site findByUrl(String url) {
        try {
            return jdbcTemplate.queryForObject("select * from site where url = ?", rowMapper, url);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    
    public List<Site> findAll() {
        try {
            return jdbcTemplate.query("select * from site", rowMapper);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }
    
}
