package main.repository;

import main.model.Lemma;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class LemmaRepository {
    
    private final RowMapper<Lemma> rowMapper = (rs, rowNum) -> {
        Lemma lemma = new Lemma();
        lemma.setId(rs.getInt("id"));
        lemma.setLemma(rs.getString("lemma"));
        lemma.setFrequency(rs.getInt("frequency"));
        lemma.setSiteId(rs.getInt("site_id"));
        return lemma;
    };
    
    private final JdbcTemplate jdbcTemplate;
    
    
    @Autowired
    public LemmaRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    
    public void incrementInsertLemmas(List<Lemma> lemmas) {
        
        StringBuffer sql = new StringBuffer("insert into lemma(lemma, frequency, site_id) values ");
        lemmas.forEach(lemma -> sql.append("('")
                .append(lemma.getLemma())
                .append("',1,")
                .append(lemma.getSiteId())
                .append("),"));
        sql.deleteCharAt(sql.length() - 1);
        sql.append(" on duplicate key update frequency = frequency + 1");
        
        jdbcTemplate.update(sql.toString());
    }
    
    public Lemma findById(int lemmaId) {
        try {
            return jdbcTemplate.queryForObject("select * from lemma where id = ?", rowMapper, lemmaId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    
    public Lemma findByLemmaAndSiteId(String lemma, int siteId) {
        try {
            return jdbcTemplate.queryForObject("select * from lemma where lemma = ? and site_id = ?",
                    rowMapper, lemma, siteId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    
    public List<Lemma> findByLemmas(List<String> lemmas) {
    
        String insert = String.join(",", Collections.nCopies(lemmas.size(), "?"));
        String sql = String.format("select * from lemma where lemma in (%s)", insert);
        
        try {
            return jdbcTemplate.query(sql, rowMapper, lemmas.toArray());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }
    
    public List<Lemma> findByLemmasAndSiteId(List<String> lemmas, int siteId) {
        
        String insert = String.join(",", Collections.nCopies(lemmas.size(), "?"));
        String sql = String.format("select * from lemma where site_id = %d and lemma in (%s)", siteId, insert);
        
        try {
            return jdbcTemplate.query(sql, rowMapper, lemmas.toArray());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }
    
    public Integer countBySiteId(int siteId) {
        return jdbcTemplate.queryForObject("select count(*) from lemma where site_id = ?",
                Integer.class, siteId);
    }
    
    public void decrementAndUpdateLemma(String lemma, int siteId) {
        jdbcTemplate.update("update lemma set frequency = frequency - 1 where lemma = ? and site_id = ?",
                lemma, siteId);
        
    }
    
    public void deleteBySiteId(int siteId) {
        jdbcTemplate.update("delete from lemma where site_id = ?", siteId);
    }
    
    public void deleteByLemmaAndSiteId(String lemma, int siteId) {
        jdbcTemplate.update("delete from lemma where lemma = ? and site_id = ?", lemma, siteId);
    }
}
