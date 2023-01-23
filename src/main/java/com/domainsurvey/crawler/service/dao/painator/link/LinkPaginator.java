package com.domainsurvey.crawler.service.dao.painator.link;

import lombok.extern.log4j.Log4j2;

import static org.jooq.SQLDialect.POSTGRES_9_5;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import com.domainsurvey.crawler.dto.LinkData;
import com.domainsurvey.crawler.model.filter.LinkFilterConfig;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.service.dao.mapper.LinkDataMapper;
import com.domainsurvey.crawler.service.dao.painator.Paginator;
import com.domainsurvey.crawler.service.dao.painator.model.PaginatorResult;
import com.domainsurvey.crawler.service.filter.FilterParserService;
import com.domainsurvey.crawler.service.table.type.TableType;
import com.domainsurvey.crawler.web.ws.model.request.message.DomainPaginationRequest;

@Log4j2
@Component
public abstract class LinkPaginator implements Paginator<LinkData> {
    public DSLContext dslContext = DSL.using(POSTGRES_9_5);

    @Autowired
    protected QueryExecutor queryExecutor;

    public RowMapper<LinkData> getRowMapper() {
        return new LinkDataMapper();
    }

    public abstract SelectJoinStep<Record> listQuery(DomainPaginationRequest domainPaginationRequest);

    public abstract SelectJoinStep<Record> countQuery(DomainPaginationRequest domainPaginationRequest);

    protected abstract SelectJoinStep<? extends Record> baseQuery(DomainPaginationRequest domainPaginationRequest, SelectJoinStep<? extends Record> query);

    List<Field<?>> fields() {
        return new ArrayList<>(Arrays.asList(
                field(name(TableType.NODE.getValue(), "id")).as("id"),
                field(name(TableType.NODE.getValue(), "url")).as("url"),
                field(name(TableType.NODE.getValue(), "depth")).as("depth"),
                field(name(TableType.NODE.getValue(), "robots_valid")).as("robots_valid"),
                field(name(TableType.NODE.getValue(), "redirected_links")).as("redirected_links"),
                field(name(TableType.EDGE.getValue(), "meta_data")).as("meta_data"),
                field(name(TableType.PAGE.getValue(), "status_code")).as("status_code")
        ));
    }

    @Override
    public PaginatorResult<LinkData> list(DomainPaginationRequest domainPaginationRequest) {
        SelectJoinStep<Record> listQuery = listQuery(domainPaginationRequest);
        SelectJoinStep<Record> countQuery = countQuery(domainPaginationRequest);

        int offset = domainPaginationRequest.getPageSize() * (domainPaginationRequest.getPage() - 1);
        int limit = domainPaginationRequest.getPageSize() == -1 ? Integer.MAX_VALUE : domainPaginationRequest.getPageSize();
        Query query = listQuery.limit(limit).offset(offset);

        String listSql = query.toString();
        String countSql = countQuery.toString();

        log.info(listSql);

        List<LinkData> data = queryExecutor.queryList(listSql, getRowMapper());

        int total = queryExecutor.queryForInteger(countSql);

        return new PaginatorResult<>(data, total, total);
    }

    protected String additionalDataWhere(DomainPaginationRequest domainPaginationRequest) {
        String where = "";
        if (domainPaginationRequest.getAdditionalData().containsKey("filterId")) {
            LinkFilterConfig filterConfig = FilterParserService.linkFilters.get(Integer.parseInt(domainPaginationRequest.getAdditionalData().get("filterId")));
            where += (filterConfig != null ? " AND " + filterConfig.where : "").replaceAll("%s", TableType.PAGE.getValue());
        }
        if (domainPaginationRequest.getAdditionalData().containsKey("search")) {

            String search = domainPaginationRequest.getAdditionalData().get("search");

            if (!search.isEmpty()) {
                where += " AND " + TableType.NODE.getValue() + ".url like '%" + search + "%'";
            }
        }

        return where;
    }
}