package main.repository;

import main.model.Lemma;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
        String sql = "insert into lemma(lemma, frequency, site_id) values (?, 1, ?)" +
                "on duplicate key update frequency = frequency + 1";
        
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Lemma lemma = lemmas.get(i);
                ps.setString(1, lemma.getLemma());
                ps.setInt(2, lemma.getSiteId());
            }
    
            @Override
            public int getBatchSize() {
                return lemmas.size();
            }
        });
    }
    
    public Lemma findById(int lemmaId) {
        try {
            return jdbcTemplate.queryForObject("select * from lemma where id = ?", rowMapper, lemmaId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    
    // TODO : change to List<Lemma>
    public Lemma findByLemma(String lemma) {
        try {
            return jdbcTemplate.queryForObject("select * from lemma where lemma = ?",
                    rowMapper, lemma);
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
