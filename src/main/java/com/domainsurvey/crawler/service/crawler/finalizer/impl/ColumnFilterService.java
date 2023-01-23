package com.domainsurvey.crawler.service.crawler.finalizer.impl;

import lombok.RequiredArgsConstructor;

import static com.domainsurvey.crawler.service.table.TableService.getFullTableName;

import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.service.filter.FilterParserService;
import com.domainsurvey.crawler.service.table.type.SchemaType;
import com.domainsurvey.crawler.service.table.type.TableType;

@Service
@RequiredArgsConstructor
public class ColumnFilterService {

    private final QueryExecutor queryExecutor;

    public void process(Domain domain) {
        processNonIssueFilters(domain);
    }

    public void processNonIssueFilters(Domain domain) {
        FilterParserService.dbFilterList.forEach(filter -> {
            String bodyQuery;
            if (filter.bodyQuery != null) {
                bodyQuery = filter.bodyQuery.apply(domain.getId());
            } else {
                bodyQuery = " where " + FilterParserService
                        .getWhere(filter)
                        .replaceAll("TABLE_NAME", getFullTableName(domain.getId(), SchemaType.FINAL, TableType.PAGE));
            }

            String tableName = getFullTableName(domain.getId(), SchemaType.FINAL, TableType.PAGE);

            if (!bodyQuery.equals("")) {
                String sql = String.format(
                        "update %s p_update set filters = array_append(p_update.filters, %s) %s",
                        tableName,
                        filter.id,
                        bodyQuery
                );

                queryExecutor.executeQuery(sql);
            }
        });
    }
}