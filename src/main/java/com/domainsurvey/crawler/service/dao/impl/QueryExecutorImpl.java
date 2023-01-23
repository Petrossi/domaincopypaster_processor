package com.domainsurvey.crawler.service.dao.impl;

import lombok.RequiredArgsConstructor;

import static com.domainsurvey.crawler.service.table.TableService.getFullTableName;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.service.table.type.SchemaType;
import com.domainsurvey.crawler.service.table.type.TableType;

@Service
@RequiredArgsConstructor
public class QueryExecutorImpl implements QueryExecutor {

    protected final JdbcTemplate jdbcTemplate;

    public void executeQuery(String query) {
        jdbcTemplate.execute(query);
    }

    public String queryForString(String sql) {
        return queryForString(sql, new Object[]{});
    }

    public String queryForString(String sql, Object[] args) {
        return jdbcTemplate.queryForObject(sql, args, String.class);
    }

    public Integer queryForInteger(String sql) {
        return queryForInteger(sql, new Object[]{});
    }

    public Integer queryForInteger(String sql, Object[] args) {
        return jdbcTemplate.queryForObject(sql, args, Integer.class);
    }

    public Long queryForLong(String sql) {
        return queryForLong(sql, new Object[]{});
    }

    public Long queryForLong(String sql, Object[] args) {
        return jdbcTemplate.queryForObject(sql, args, Long.class);
    }

    @Override
    public Double queryForDouble(String sql) {
        return jdbcTemplate.queryForObject(sql, new Object[]{}, Double.class);
    }

    @Override
    public <T> List<T> queryList(String sql, RowMapper<T> rowMapper) {
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper) {
        return jdbcTemplate.queryForObject(sql, rowMapper);
    }

    @Override
    public <T> List<T> queryForList(String sql, Class<T> elementType) {
        return jdbcTemplate.queryForList(sql, elementType);
    }

    @Override
    public void batchUpdate(String sql, BatchPreparedStatementSetter pss) throws DataAccessException {
        jdbcTemplate.batchUpdate(sql, pss);
    }

    @Override
    public int update(String sql, PreparedStatementSetter pss) throws DataAccessException {
        return jdbcTemplate.update(sql, pss);
    }

    public void updateTableSql(String domainId, SchemaType schemaType, TableType tableType, String sqlBody) {
        String sql = String.format("UPDATE %s %s", getFullTableName(domainId, schemaType, tableType), sqlBody);

        executeQuery(sql);
    }

    @Override
    public JdbcTemplate jdbcTemplate() {
        return jdbcTemplate;
    }
}