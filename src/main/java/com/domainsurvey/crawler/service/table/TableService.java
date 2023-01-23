package com.domainsurvey.crawler.service.table;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;
import com.domainsurvey.crawler.exception.CantCreateCrawlingTablesException;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.service.table.type.SchemaType;
import com.domainsurvey.crawler.service.table.type.TableType;

@Component
@RequiredArgsConstructor
public class TableService {

    private static final String CREATE_UNLOGGED_TABLE = "CREATE UNLOGGED TABLE IF NOT EXISTS ";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS ";

    private final QueryExecutor queryExecutor;

    public static final List<TableType> BASE_CRAWLER_TABLES = Arrays.asList(
            TableType.PAGE,
            TableType.NODE,
            TableType.EDGE
    );

    public void deleteCrawlingTables(String domainId) {
        BASE_CRAWLER_TABLES.forEach(table -> dropTable(SchemaType.PROCESS, table, domainId));
    }

    public void deleteFinalTables(String domainId) {
        BASE_CRAWLER_TABLES.forEach(table -> dropTable(SchemaType.FINAL, table, domainId));
    }

    public void deleteFinalTables(Domain domain) {
        deleteFinalTables(domain.getId());
    }

    public void deleteLastCrawlingTables(String domainId) {
        BASE_CRAWLER_TABLES.forEach(table -> dropTable(SchemaType.LAST, table, domainId));
    }

    public void deleteAllCrawlingTables(String domainId) {
        BASE_CRAWLER_TABLES.forEach(table -> dropTable(SchemaType.PROCESS, table, domainId));
    }

    public void createCrawlerProcessTables(Domain domain) throws CantCreateCrawlingTablesException {
        try {
            createPageTable(domain);
            createLinkTable(domain);
            createEdgeTable(domain);
        } catch (Exception e) {
            deleteAllCrawlingTables(domain.getId());
            e.printStackTrace();
            throw new CantCreateCrawlingTablesException();
        }
    }

    public void createPageTable(Domain domain) {
        createPageTable(domain.getId());
    }

    public void createPageTable(String domainId) {
        TableType tableType = TableType.PAGE;

        String fullTableName = getFullTableName(domainId, SchemaType.PROCESS, tableType);

        if (!tableExists(fullTableName)) {
            String createTableSql =
                    CREATE_UNLOGGED_TABLE + fullTableName + " (LIKE public." + tableType + " EXCLUDING INDEXES) WITH (autovacuum_enabled=false);" +
                            "ALTER TABLE " + fullTableName + "  ALTER COLUMN weight set DEFAULT 0.25;" +
                            "ALTER TABLE " + fullTableName + " ADD PRIMARY KEY (id);";
            queryExecutor.executeQuery(createTableSql);

            Arrays.asList(
                    "CREATE INDEX idx_%s_filters_%s on %s USING GIN (filters);",
                    "CREATE INDEX idx_%s_hmd_title_%s ON %s ((hashed_meta_data -> 'title'));",
                    "CREATE INDEX idx_%s_hmd_description_%s ON %s ((hashed_meta_data -> 'description'));",
                    "CREATE INDEX idx_%s_hmd_h1_%s ON %s ((hashed_meta_data -> 'h1'));",
                    "CREATE INDEX idx_%s_hmd_canonical_%s ON %s ((hashed_meta_data -> 'canonical'));"
            ).forEach(sql -> queryExecutor.executeQuery(String.format(sql, tableType, domainId, fullTableName)));
        }
    }

    public void createLinkTable(Domain domain) {
        createLinkTable(domain.getId());
    }

    public void createLinkTable(String domainId) {
        TableType tableType = TableType.NODE;

        String fullTableName = getFullTableName(domainId, SchemaType.PROCESS, tableType);

        if (!tableExists(fullTableName)) {
            String sql =
                    CREATE_UNLOGGED_TABLE + fullTableName + " (like public." + tableType + " EXCLUDING INDEXES);" +
                            "ALTER TABLE " + fullTableName + " ADD PRIMARY KEY (id);";

            queryExecutor.executeQuery(sql);
        }
    }

    public void createEdgeTable(Domain domain) {
        createEdgeTable(domain.getId());
    }

    public void createEdgeTable(String domainId) {
        TableType tableType = TableType.EDGE;

        String fullTableName = getFullTableName(domainId, SchemaType.PROCESS, tableType);

        if (!tableExists(fullTableName)) {
            String sql =
                    CREATE_UNLOGGED_TABLE + fullTableName + " (like public." + tableType + " EXCLUDING INDEXES);" +
                            "ALTER TABLE " + fullTableName + " ADD PRIMARY KEY (id);" +
                            "ALTER TABLE " + fullTableName + " ALTER COLUMN id set DEFAULT gen_random_uuid();";

            queryExecutor.executeQuery(sql);

            Arrays.asList(
                    "CREATE INDEX idx_%s_%s_source_id_idx ON %s (source_id);",
                    "CREATE INDEX idx_%s_%s_target_id_idx ON %s (target_id);"
            ).forEach(s -> queryExecutor.executeQuery(String.format(s, tableType, domainId, fullTableName)));
        }
    }

    public boolean tableExists(Domain domain, SchemaType schemaType, TableType tableType) {
        return tableExists(domain.getId(), schemaType, tableType);
    }

    public boolean tableExists(String domainId, SchemaType schemaType, TableType tableType) {
        String fullTableName = getFullTableName(domainId, schemaType, tableType);

        return tableExists(fullTableName);
    }

    public boolean tableExists(String fullTableName) {
        String sql = String.format("SELECT to_regclass('%s')", fullTableName);

        return queryExecutor.queryForString(sql) != null;
    }

    public boolean columnExists(Domain domain, TableType tableName, String columnName) {
        String sql =
                "SELECT " +
                        "   count(*) " +
                        "FROM information_schema.columns " +
                        "WHERE concat(table_schema, '.',table_name) = '" + tableName + "' and column_name='" + columnName + "' " +
                        "limit 1";

        return queryExecutor.queryForInteger(sql) > 0;
    }

    public void dropTable(SchemaType schemaType, TableType tableType, String domainId) {
        String table = getFullTableName(domainId, schemaType, tableType);

        String sql = DROP_TABLE + table + " CASCADE;";

        queryExecutor.executeQuery(sql);
    }

    public void truncateTable(SchemaType schemaType, TableType tableType, String domainId) {
        String table = getFullTableName(domainId, schemaType, tableType);

        String sql = String.format("truncate table %s.%s_%s;", schemaType.getValue(), tableType.getValue(), domainId);

        queryExecutor.executeQuery(sql);
    }

    public void moveFromSchemaToSchema(String domainId, SchemaType from, SchemaType to, TableType table) {
        String fromFullTableName = getFullTableName(domainId, from, table);

        queryExecutor.executeQuery(String.format("ALTER TABLE IF EXISTS %s SET SCHEMA %s", fromFullTableName, to));
    }

    public static String getFullTableName(String domainId, SchemaType schemaType, TableType tableType) {
        return schemaType + "." + tableType + "_" + domainId;
    }

    public void copyDomainsFinalPageTables(Domain fromDomain, Domain toDomain) {
    }
}