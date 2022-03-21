package main.repository;

import main.model.Index;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

@Repository
public class IndexRepository {
    
    private final RowMapper<Index> rowMapper = (rs, rowNum) -> {
        Index index = new Index();
        index.setId(rs.getInt("id"));
        index.setPageId(rs.getInt("page_id"));
        index.setLemmaId(rs.getInt("lemma_id"));
        index.setRank(rs.getFloat("_rank"));
        return index;
    };
    
    private final JdbcTemplate jdbcTemplate;
    
    @Autowired
    public IndexRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    
    @Transactional
    public void saveAll(List<Index> indexes) {
        indexes.forEach(index -> {
            jdbcTemplate.update("insert into _index(page_id, lemma_id, _rank) values (?, ?, ?)",
                    index.getPageId(), index.getLemmaId(), index.getRank());
        });
    }
    
    public List<Index> findByPageId(int pageId) {
        try {
            return jdbcTemplate.query("select * from _index where page_id = ?", rowMapper, pageId);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }
    
    public List<Index> findByLemmaId(int lemmaId) {
        try {
            return jdbcTemplate.query("select * from _index where lemma_id = ?", rowMapper, lemmaId);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }
    
    @Transactional
    public void deleteByPageId(int pageId) {
        jdbcTemplate.update("delete from _index where page_id = ?", pageId);
    }
    
}
