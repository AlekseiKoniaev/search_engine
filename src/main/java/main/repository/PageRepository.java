package main.repository;

import main.model.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

@Repository
public class PageRepository {
    
    private final RowMapper<Page> rowMapper = (rs, rowNum) -> {
        Page page = new Page();
        page.setId(rs.getInt("id"));
        page.setPath(rs.getString("path"));
        page.setCode(rs.getInt("code"));
        page.setContent(rs.getString("content"));
        page.setSiteId(rs.getInt("site_id"));
        return page;
    };
    
    private final JdbcTemplate jdbcTemplate;
    
    
    @Autowired
    public PageRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    
    public void save(Page page) {
        jdbcTemplate.update("insert into page(path, code, content, site_id) values (?, ?, ?, ?)",
                page.getPath(), page.getCode(), page.getContent(), page.getSiteId());
    }
    
    public Page findById(int pageId) {
        try {
            return jdbcTemplate.queryForObject("select * from page where id = ?", rowMapper, pageId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    
    public Page findByPathAndSiteId(String path, int siteId) {
        try {
            return jdbcTemplate.queryForObject("select * from page where path = ? and site_id = ?",
                    rowMapper, path, siteId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    
    public List<Page> findBySiteId(int siteId) {
        try {
            return jdbcTemplate.query("select * from page where site_id = ?", rowMapper, siteId);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }
    
    public Integer count() {
        return jdbcTemplate.queryForObject("select count(*) from page", Integer.class);
    }
    
    public Integer countBySiteId(int siteId) {
        return jdbcTemplate.queryForObject("select count(*) from page where site_id = ?",
                Integer.class, siteId);
    }
    
    public void deleteBySiteId(int siteId) {
        jdbcTemplate.update("delete from page where site_id = ?", siteId);
    }
    
    public void deleteByPathAndSiteId(String path, int siteId) {
        jdbcTemplate.update("delete from page where path = ? and site_id = ?", path, siteId);
    }
}
