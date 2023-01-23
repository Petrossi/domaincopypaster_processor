package com.domainsurvey.crawler.service.crawler.finalizer.impl.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import static com.domainsurvey.crawler.service.table.TableService.getFullTableName;

import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.type.FinalizerStatus;
import com.domainsurvey.crawler.service.crawler.finalizer.CrawlingFinalizerProcessor;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.service.table.TableService;
import com.domainsurvey.crawler.service.table.type.SchemaType;
import com.domainsurvey.crawler.service.table.type.TableType;

@Service
@RequiredArgsConstructor
@Log4j2
public class CountPageRankProgressFinalizerProcessor implements CrawlingFinalizerProcessor {

    private final QueryExecutor queryExecutor;

    public void process(Domain domain) {
        log.info("start: " + domain.getId());

        int maxIterations = 3;

        String tableName = TableService.getFullTableName(domain.getId(), SchemaType.PROCESS, TableType.PAGE);

        IntStream.range(0, maxIterations).forEach(i -> {
            String sqlBodyTemplate = " n SET " +
                    " weight = round(1.0 - 0.85 + COALESCE(x.transfer_weight, 0.0), 3)" +
                    " FROM (" +
                    "   SELECT" +
                    "       n.id as id," +
                    "       l.robots_valid as robots_valid," +
                    "       round(n.weight / n.incoming_count_total * 0.85, 3) as transfer_weight" +
                    "   FROM %s n" +
                    "   INNER JOIN %s l on l.id = n.id" +
                    "   WHERE n.incoming_count_total > 0 AND l.robots_valid = true" +
                    "  ) as x" +
                    " WHERE x.id = n.id;";

            String sqlBody = String.format(
                    sqlBodyTemplate,
                    tableName,
                    TableService.getFullTableName(domain.getId(), SchemaType.PROCESS, TableType.NODE),
                    TableService.getFullTableName(domain.getId(), SchemaType.PROCESS, TableType.EDGE)
            );
            String sql = String.format("UPDATE %s %s", getFullTableName(domain.getId(), SchemaType.PROCESS, TableType.PAGE), sqlBody);

            queryExecutor.executeQuery(sql);
        });

        finalizeCount(domain);

        log.info("finished : " + domain.getId());
    }

    private void finalizeCount(Domain domain) {
        normalizeNodesWeight(domain);

        String sqlBodyTemplate =
                " n " +
                        "SET weight = 0" +
                        "FROM ( " +
                        "         SELECT n.id " +
                        "         FROM %s n " +
                        "         INNER JOIN %s l on l.id = n.id " +
                        "         where l.depth > 0 and (n.incoming_count_total = 0 or l.robots_valid = false) " +
                        " ) x " +
                        " WHERE x.id = n.id;";

        String sqlBody = String.format(
                sqlBodyTemplate,
                TableService.getFullTableName(domain.getId(), SchemaType.PROCESS, TableType.PAGE),
                TableService.getFullTableName(domain.getId(), SchemaType.PROCESS, TableType.NODE)
        );

        queryExecutor.updateTableSql(domain.getId(), SchemaType.PROCESS, TableType.PAGE, sqlBody);
    }

    private void normalizeNodesWeight(Domain domain) {
        String maxWeightSql = "SELECT max(weight) FROM " + getFullTableName(domain.getId(), SchemaType.PROCESS, TableType.PAGE);

        double maxWeight = 0;
        try {
            maxWeight = queryExecutor.queryForDouble(maxWeightSql);
        } catch (Exception ignored) {
        }

        if (maxWeight > 0) {
            String sqlBody = " set weight = (weight / " + maxWeight + "  * 100) where weight > 0;";

            queryExecutor.updateTableSql(domain.getId(), SchemaType.PROCESS, TableType.PAGE, sqlBody);
        }
    }

    @Override
    public FinalizerStatus getNextStatus() {
        return FinalizerStatus.IMPORT_INTO_FINAL_TABLES;
    }
}