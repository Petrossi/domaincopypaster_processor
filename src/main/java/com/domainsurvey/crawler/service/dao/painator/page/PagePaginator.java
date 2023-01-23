package com.domainsurvey.crawler.service.dao.painator.page;

import com.domainsurvey.crawler.dto.PageData;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.service.dao.mapper.PageDataMapper;
import com.domainsurvey.crawler.service.dao.painator.Paginator;
import com.domainsurvey.crawler.service.dao.painator.model.PaginatorResult;
import com.domainsurvey.crawler.service.table.TableService;
import com.domainsurvey.crawler.service.table.type.SchemaType;
import com.domainsurvey.crawler.service.table.type.TableType;
import com.domainsurvey.crawler.web.ws.model.request.message.DomainPaginationRequest;
import lombok.extern.log4j.Log4j2;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.SelectSelectStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.domainsurvey.crawler.service.filter.FilterParserService.ALL_PAGES_FILTER_ID;
import static org.jooq.SQLDialect.POSTGRES_9_5;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

@Log4j2
@Component
public class PagePaginator implements Paginator<PageData> {

    @Autowired
    protected QueryExecutor queryExecutor;

    public RowMapper getRowMapper() {
        return new PageDataMapper();
    }

    public DSLContext dslContext = DSL.using(POSTGRES_9_5);

    List<Field<?>> fields() {
        return new ArrayList<>(Arrays.asList(
                field(name(TableType.PAGE.getValue(), "id")).as("id"),
                field(name(TableType.PAGE.getValue(), "status_code")).as("status_code"),
                field(name(TableType.PAGE.getValue(), "weight")).as("weight"),
                field(name(TableType.PAGE.getValue(), "incoming_count_total")).as("incoming_count_total"),
                field(name(TableType.PAGE.getValue(), "filters")).as("filters"),
                field(name(TableType.PAGE.getValue(), "saved_meta_data")).as("saved_meta_data"),
                field(name(TableType.NODE.getValue(), "url")).as("url"),
                field(name(TableType.NODE.getValue(), "redirected_links")).as("redirected_links"),
                field(name(TableType.NODE.getValue(), "robots_valid")).as("robots_valid")
        ));
    }

    protected String tableName(String id) {
        return TableService.getFullTableName(id, SchemaType.FINAL, TableType.PAGE);
    }

    public PaginatorResult<PageData> list(DomainPaginationRequest domainPaginationRequest) {
        List<PageData> data = getData(domainPaginationRequest);
        int total = getTotal(domainPaginationRequest);
        int filtered = getFiltered(domainPaginationRequest);
        return new PaginatorResult<>(data, total, filtered);
    }

    protected List<PageData> getData(DomainPaginationRequest domainPaginationRequest) {
        int offset = domainPaginationRequest.getPageSize() * (domainPaginationRequest.getPage() - 1);
        int limit = domainPaginationRequest.getPageSize() == -1 ? Integer.MAX_VALUE : domainPaginationRequest.getPageSize();

        SelectJoinStep<Record> fromTable = getFromTable(domainPaginationRequest.getId());

        SelectJoinStep<Record> whereTable = addWhere(fromTable, domainPaginationRequest);

        Query query = whereTable.limit(limit).offset(offset);

        String sql = query.toString();

        log.info("execute: {}", sql);

        return (List<PageData>) queryExecutor.queryList(sql, getRowMapper());
    }

    public SelectJoinStep<Record> getFromTable(String domainId) {
        String linkTableName = TableService.getFullTableName(domainId, SchemaType.FINAL, TableType.NODE);

        return dslContext.select(fields())
                .from(table(tableName(domainId)).as(TableType.PAGE.getValue()))
                .innerJoin(table(linkTableName).as(TableType.NODE.getValue()))
                .on(String.format("%s.id = %s.id", TableType.PAGE.getValue(), TableType.NODE.getValue()));
    }

    protected <T extends Record> SelectJoinStep<T> addFrom(SelectSelectStep<T> selectStep, DomainPaginationRequest domainPaginationRequest) {
        String linkTableName = TableService.getFullTableName(domainPaginationRequest.getId(), SchemaType.FINAL, TableType.NODE);

        return selectStep.from(table(tableName(domainPaginationRequest.getId())).as(TableType.PAGE.getValue()))
                .innerJoin(table(linkTableName).as(TableType.NODE.getValue()))
                .on(String.format("%s.id = %s.id", TableType.PAGE.getValue(), TableType.NODE.getValue()))
                ;
    }

    protected <T extends Record> SelectJoinStep<T> addWhere(SelectJoinStep<T> fromTable, DomainPaginationRequest domainPaginationRequest) {
        if (domainPaginationRequest.getAdditionalData().containsKey("filters")) {
            String[] filtersFromRequest = domainPaginationRequest.getAdditionalData().get("filters").split(",");

            List<String> filters = new ArrayList<>();
            List<String> conditions = new ArrayList<>();

            for (String filterId : filtersFromRequest) {
                if (Integer.parseInt(filterId) == ALL_PAGES_FILTER_ID) {
                    filters.clear();
                } else {
                    filters.add(filterId);
                }
            }

            if (!filters.isEmpty()) {
                conditions.add(String.format("ARRAY [%s] :: int [] && page.filters", String.join(",", filters)));
            }

            boolean containsSearch = domainPaginationRequest.getAdditionalData().containsKey("search") && !domainPaginationRequest.getAdditionalData().get("search").isEmpty();

            if (!conditions.isEmpty() || containsSearch) {

                String whereCondition = "";
                String whereSearch = "";

                if (!conditions.isEmpty()) {
                    conditions = conditions
                            .stream()
                            .map(condition -> condition.replaceAll("fp.", TableType.PAGE.getValue() + ".").replaceAll("fp.", TableType.PAGE.getValue() + "."))
                            .collect(Collectors.toList());
                    whereCondition = "(" + String.join(" or ", conditions) + ")";
                }

                if (containsSearch) {
                    String search = domainPaginationRequest.getAdditionalData().get("search");

                    whereSearch = TableType.NODE.getValue() + ".url like '%" + search + "%'";
                }

                String where = Stream.of(whereCondition, whereSearch).filter(f -> !f.isEmpty()).collect(Collectors.joining(" and "));

                if (!where.isEmpty()) {
                    fromTable.where(where);
                }
            }
        }

        if (domainPaginationRequest.getAdditionalData().containsKey("pageId")) {
            fromTable.where(TableType.NODE.getValue() + ".id = " + domainPaginationRequest.getAdditionalData().get("pageId"));
        }

        return fromTable;
    }

    protected int getFiltered(DomainPaginationRequest domainPaginationRequest) {
        String sql = addWhere(addFrom(dslContext.selectCount(), domainPaginationRequest), domainPaginationRequest).toString();

        return queryExecutor.queryForInteger(sql);
    }

    protected int getTotal(DomainPaginationRequest domainPaginationRequest) {
        Query query = addFrom(dslContext.selectCount(), domainPaginationRequest);

        String sql = query.toString();

        return queryExecutor.queryForInteger(sql);
    }
}