package main.repository;

import main.model.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

@Repository
public class FieldRepository {
    
    private final RowMapper<Field> rowMapper = (rs, rowNum) -> {
        Field field = new Field();
        field.setId(rs.getInt("id"));
        field.setName(rs.getString("name"));
        field.setSelector(rs.getString("selector"));
        field.setWeight(rs.getFloat("weight"));
        return field;
    };
    
    private final JdbcTemplate jdbcTemplate;
    
    @Autowired
    public FieldRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    
    public List<Field> findAll() {
        try {
            return jdbcTemplate.query("select * from field", rowMapper);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }
}