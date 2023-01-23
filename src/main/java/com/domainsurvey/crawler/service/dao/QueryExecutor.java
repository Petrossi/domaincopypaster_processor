package com.domainsurvey.crawler.service.dao;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import com.domainsurvey.crawler.service.table.type.SchemaType;
import com.domainsurvey.crawler.service.table.type.TableType;

public interface QueryExecutor {
    void executeQuery(String query);

    String queryForString(String sql);

    String queryForString(String sql, Object[] args);

    Integer queryForInteger(String sql);

    Integer queryForInteger(String sql, Object[] args);

    Long queryForLong(String sql);

    Long queryForLong(String sql, Object[] args);

    Double queryForDouble(String sql);

    <T> List<T> queryList(String sql, RowMapper<T> rowMapper);

    <T> T queryForObject(String sql, RowMapper<T> rowMapper);

    <T> List<T> queryForList(String sql, Class<T> elementType);

    void updateTableSql(String domainId, SchemaType schemaType, TableType tableType, String sqlBody);

    void batchUpdate(String sql, BatchPreparedStatementSetter pss) throws DataAccessException;

    int update(String sql, PreparedStatementSetter pss) throws DataAccessException;

    JdbcTemplate jdbcTemplate();
}