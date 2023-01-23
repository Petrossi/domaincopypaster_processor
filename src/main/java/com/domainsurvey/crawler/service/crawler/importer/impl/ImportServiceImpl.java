package com.domainsurvey.crawler.service.crawler.importer.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.link.Edge;
import com.domainsurvey.crawler.model.link.Node;
import com.domainsurvey.crawler.model.page.Page;
import com.domainsurvey.crawler.service.crawler.importer.InsertImportService;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.service.table.TableService;
import com.domainsurvey.crawler.service.table.type.SchemaType;
import com.domainsurvey.crawler.service.table.type.TableType;
import com.domainsurvey.crawler.utils.JsonConverter;

@Service
@RequiredArgsConstructor
@Log4j2
public class ImportServiceImpl implements InsertImportService {

    private final QueryExecutor queryExecutor;

    @Override
    public void importPages(List<Page> pages, Domain domain) {
        String insertString = "insert INTO %s (%s) values(%s)";

        String sql = String.format(
                insertString,
                TableService.getFullTableName(domain.getId(), SchemaType.PROCESS, TableType.PAGE),
                "id, status_code, score, filters, saved_meta_data, hashed_meta_data",
                "?, ?, ?, ?::integer[], ?::jsonb, ?::jsonb"
        );

        queryExecutor.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Page page = pages.get(i);

                String filters = String.format(
                        "{%s}",
                        page.getFilters().stream().map(String::valueOf).collect(Collectors.joining(", "))
                );

                String pageMetaData = "{}";
                String hashedMetaData = "{}";

                if (page.getSavedMetaData() != null) {
                    pageMetaData = JsonConverter.convertToJson(page.getSavedMetaData());
                    hashedMetaData = JsonConverter.convertToJson(page.getSavedMetaData().toHashedMetaData());
                }

                ps.setLong(1, page.getId());
                ps.setShort(2, page.getStatusCode());
                ps.setShort(3, page.getScore());
                ps.setString(4, filters);
                ps.setString(5, pageMetaData);
                ps.setString(6, hashedMetaData);
            }

            @Override
            public int getBatchSize() {
                return pages.size();
            }
        });
    }

    @Override
    public void importLinks(List<Node> nodes, Domain domain) {
        String insertString = "insert INTO %s (%s) values(%s)";

        String sql = String.format(
                insertString,
                TableService.getFullTableName(domain.getId(), SchemaType.PROCESS, TableType.NODE),
                "id, url, type, depth, robots_valid, redirect_count, redirected_links",
                "?, ?, ?, ?, ?, ?, ?::jsonb"
        );

        queryExecutor.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Node node = nodes.get(i);

                String redirectedLinks = String.format(
                        "[%s]",
                        node.getRedirectedLinks().stream().map(JsonConverter::convertToJson).collect(Collectors.joining(", "))
                );

                ps.setLong(1, node.getId());
                ps.setString(2, node.getUrl());
                ps.setByte(3, node.getType().getValue());
                ps.setShort(4, node.getDepth());
                ps.setBoolean(5, node.isRobotsValid());
                ps.setByte(6, node.getRedirectCount());
                ps.setString(7, redirectedLinks);
            }

            @Override
            public int getBatchSize() {
                return nodes.size();
            }
        });
    }

    @Override
    public void importEdges(List<Edge> edges, Domain domain) {
        String insertString = "insert INTO %s (%s) values(%s)";

        String sql = String.format(
                insertString,
                TableService.getFullTableName(domain.getId(), SchemaType.PROCESS, TableType.EDGE),
                "target_id, source_id, meta_data",
                "?, ?, ?::jsonb"
        );

        queryExecutor.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Edge edge = edges.get(i);

                String metaData = "{}";
                if (edge.getMetaData() != null) {
                    metaData = JsonConverter.convertToJson(edge.getMetaData());
                }

                ps.setLong(1, edge.getTargetId());
                ps.setLong(2, edge.getSourceId());
                ps.setString(3, metaData);
            }

            @Override
            public int getBatchSize() {
                return edges.size();
            }
        });
    }
}