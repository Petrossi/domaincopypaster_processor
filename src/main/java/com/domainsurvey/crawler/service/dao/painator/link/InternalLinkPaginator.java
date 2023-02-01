package com.domainsurvey.crawler.service.dao.painator.link;

import com.domainsurvey.crawler.model.type.NodeType;
import com.domainsurvey.crawler.service.table.TableService;
import com.domainsurvey.crawler.service.table.type.SchemaType;
import com.domainsurvey.crawler.service.table.type.TableType;
import com.domainsurvey.crawler.web.ws.model.request.message.DomainPaginationRequest;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.SelectJoinStep;
import org.springframework.stereotype.Component;

import static org.jooq.impl.DSL.table;

@Component
public class InternalLinkPaginator extends LinkPaginator {

    @Override
    public SelectJoinStep<Record> listQuery(DomainPaginationRequest domainPaginationRequest) {
        String edgeTableName = TableService.getFullTableName(domainPaginationRequest.getId(), SchemaType.FINAL, TableType.EDGE);
        SelectJoinStep<Record> query = dslContext.select(fields()).from(table(edgeTableName).as(TableType.EDGE.getValue()));

        return (SelectJoinStep<Record>) baseQuery(domainPaginationRequest, query);
    }

    @Override
    public SelectJoinStep<Record> countQuery(DomainPaginationRequest domainPaginationRequest) {
        String edgeTableName = TableService.getFullTableName(domainPaginationRequest.getId(), SchemaType.FINAL, TableType.EDGE);

        SelectJoinStep<Record1<Integer>> query = dslContext.selectCount().from(table(edgeTableName).as(TableType.EDGE.getValue()));

        return (SelectJoinStep<Record>) baseQuery(domainPaginationRequest, query);
    }


    @Override
    protected SelectJoinStep<? extends Record> baseQuery(DomainPaginationRequest domainPaginationRequest, SelectJoinStep<? extends Record> query) {
        String linkTableName = TableService.getFullTableName(domainPaginationRequest.getId(), SchemaType.FINAL, TableType.NODE);
        String pageTableName = TableService.getFullTableName(domainPaginationRequest.getId(), SchemaType.FINAL, TableType.PAGE);

        query
                .innerJoin(table(linkTableName).as(TableType.NODE.getValue()))
                .on(String.format("%s.target_id = %s.id", TableType.EDGE.getValue(), TableType.NODE.getValue()))
                .leftJoin(table(pageTableName).as(TableType.PAGE.getValue()))
                .on(String.format("%s.id = %s.id", TableType.PAGE.getValue(), TableType.NODE.getValue()))
                .innerJoin(table(linkTableName).as("source_" + TableType.NODE.getValue()))
                .on(String.format("source_%s.id = %s.source_id", TableType.NODE.getValue(), TableType.EDGE.getValue()))
        ;

        String id = domainPaginationRequest.getAdditionalData().get("id");
        var type = domainPaginationRequest.getAdditionalData().getOrDefault("type", String.valueOf(NodeType.INTERNAL.getValue()));
        String additionalDataWhere = additionalDataWhere(domainPaginationRequest);

        query.where(String.format(
                "%s.source_id = %s and %s.type = %s %s",
                TableType.EDGE.getValue(), id, TableType.NODE.getValue(), type, additionalDataWhere
        ));

        return query;
    }
}