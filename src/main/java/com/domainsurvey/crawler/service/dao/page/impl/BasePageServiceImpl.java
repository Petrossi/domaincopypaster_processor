package com.domainsurvey.crawler.service.dao.page.impl;

import org.springframework.beans.factory.annotation.Autowired;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.type.FilterImportance;
import com.domainsurvey.crawler.model.type.NodeType;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.service.dao.page.PageService;
import com.domainsurvey.crawler.service.table.TableService;
import com.domainsurvey.crawler.service.table.type.SchemaType;
import com.domainsurvey.crawler.service.table.type.TableType;

public abstract class BasePageServiceImpl implements PageService {

    @Autowired
    protected QueryExecutor queryExecutor;

    protected abstract SchemaType schemaType();

    protected String tableName(String domainId) {
        return TableService.getFullTableName(domainId, schemaType(), TableType.PAGE);
    }

    @Override
    public long countTotal(Domain domain) {
        return queryExecutor.queryForLong(String.format("SELECT count(*) FROM %s", tableName(domain.getId())));
    }

    @Override
    public long countInternalTotal(Domain domain) {
        String nodeTable = TableService.getFullTableName(domain.getId(), schemaType(), TableType.NODE);

        String sql = " SELECT count(p.*) FROM %s p" +
                " INNER JOIN %s n on n.id = p.id" +
                " where n.type = %s";
        return queryExecutor.queryForLong(String.format(sql, tableName(domain.getId()), nodeTable, NodeType.INTERNAL.getValue()));
    }

    @Override
    public long countBadStatusCode(Domain domain) {
        return 0;
    }

    @Override
    public long countByFilterImportance(Domain domain, FilterImportance filterImportance) {
        String sql = String.format(
                "SELECT count(*) FROM %s where %s = true",
                tableName(domain.getId()), filterImportance
        );

        return queryExecutor.queryForLong(sql);
    }

    @Override
    public long countNoIssue(Domain domain) {
        String sql = String.format(
                "SELECT count(*) FROM %s where error = false and warning = false and notice = false",
                tableName(domain.getId())
        );

        return queryExecutor.queryForLong(sql);
    }

    @Override
    public double countTotalScore(Domain domain) {
        String sql = String.format("SELECT sum(score) FROM %s", tableName(domain.getId()));

        return queryExecutor.queryForDouble(sql);
    }
}