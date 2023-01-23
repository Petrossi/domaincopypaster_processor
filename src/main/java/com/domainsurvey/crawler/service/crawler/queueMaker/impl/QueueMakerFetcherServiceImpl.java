package com.domainsurvey.crawler.service.crawler.queueMaker.impl;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.page.Page;
import com.domainsurvey.crawler.model.type.VisitedStatus;
import com.domainsurvey.crawler.service.crawler.queueMaker.QueueMakerFetcherService;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.service.dao.mapper.PageInQueueRowMapper;
import com.domainsurvey.crawler.service.table.TableService;
import com.domainsurvey.crawler.service.table.type.SchemaType;
import com.domainsurvey.crawler.service.table.type.TableType;

@Service
@RequiredArgsConstructor
public class QueueMakerFetcherServiceImpl implements QueueMakerFetcherService {

    private final QueryExecutor queryExecutor;

    public List<Page> getPagesByLimit(Domain domain, long limit) {
        if (limit <= 0) {
            return new ArrayList<>();
        }

        String sql = getSQL(domain, limit);

        return queryExecutor.queryList(sql, new PageInQueueRowMapper());
    }

    private String getSQL(Domain domain, long limit) {
        String tableName = TableService.getFullTableName(domain.getId(), SchemaType.PROCESS, TableType.PAGE);

        String template = "" +
                "UPDATE %s \n" +
                "SET visited = %s\n" +
                "WHERE id IN (\n" +
                "    SELECT id from %s WHERE visited = %s " +
                "    ORDER BY depth ASC, created_timestamp ASC " +
                "    LIMIT %s" +
                ")\n" +
                "RETURNING *;\n";

        return String.format(
                template,
                tableName, VisitedStatus.IN_PROGRESS, tableName, VisitedStatus.NOT_VISITED, limit
        );
    }
}