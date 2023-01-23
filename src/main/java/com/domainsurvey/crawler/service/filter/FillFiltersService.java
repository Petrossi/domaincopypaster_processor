package com.domainsurvey.crawler.service.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import static com.domainsurvey.crawler.model.type.FilterImportance.ISSUE;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.domain.DomainCrawlingInfo;
import com.domainsurvey.crawler.model.filter.DomainFilter;
import com.domainsurvey.crawler.model.filter.FilterConfig;
import com.domainsurvey.crawler.model.type.CommonVersion;
import com.domainsurvey.crawler.model.type.FilterImportance;
import com.domainsurvey.crawler.model.type.NodeType;
import com.domainsurvey.crawler.service.dao.DomainFilterService;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.service.table.TableService;
import com.domainsurvey.crawler.service.table.type.SchemaType;
import com.domainsurvey.crawler.service.table.type.TableType;

@Service
@RequiredArgsConstructor
@Log4j2
public class FillFiltersService {

    private final DomainFilterService domainFilterService;
    private final QueryExecutor queryExecutor;

    public void process(Domain domain) {
        log.info("process: " + domain.getId());

        fillIssueStatus(domain);
        saveFilters(domain);
    }

    void saveFilters(Domain domain) {
        List<Integer> filters = domainFilterService.findForFillDomainCrawlingId(domain.getProcessDomainCrawlingInfo());

        String pageTableName = TableService.getFullTableName(domain.getId(), SchemaType.FINAL, TableType.PAGE);
        String nodeTableName = TableService.getFullTableName(domain.getId(), SchemaType.FINAL, TableType.NODE);

        for (FilterConfig filter : FilterParserService.dbFilterList) {
            if (filters.contains(filter.id)) {
                continue;
            }

            int count;

            if (filter.importance.equals(ISSUE)) {
                count = (int) getCountByIssueFilter(filter, domain);
            } else {
                String sql = String.format(
                        "select count(p.id) FROM %s p " +
                                "INNER JOIN %s n on n.id = p.id " +
                                "where n.type = %s and ARRAY [%d] :: int [] && p.filters",
                        pageTableName, nodeTableName, NodeType.INTERNAL.getValue(), filter.getId()
                );

                count = queryExecutor.queryForInteger(sql);
            }

            save(domain, filter, count);
        }
    }

    public void savePreCountedFilters(Map<Integer, Long> filterCounters, Domain domain) {
        for (Map.Entry<Integer, Long> filterCounter : filterCounters.entrySet()) {
            FilterConfig filter = FilterParserService.filters.get(filterCounter.getKey());

            save(domain, filter, filterCounter.getValue());
        }
    }

    private long getCountByIssueFilter(FilterConfig filterConfig, Domain domain) {
        DomainCrawlingInfo domainCrawlingInfo = domain.getProcessDomainCrawlingInfo();

        switch (filterConfig.id) {
            case FilterParserService.ALL_PAGES_FILTER_ID: {
                return domainCrawlingInfo.getTotal();
            }
            case FilterParserService.ANY_ISSUE_ID: {
                return domainCrawlingInfo.getTotal() - domainCrawlingInfo.getNoIssue();
            }
            case FilterParserService.NO_ISSUE_FILTER_ID: {
                return domainCrawlingInfo.getNoIssue();
            }
            case FilterParserService.ERROR_ISSUE_FILTER_ID: {
                return domainCrawlingInfo.getError();
            }
            case FilterParserService.WARNING_ISSUE_FILTER_ID: {
                return domainCrawlingInfo.getWarning();
            }
            case FilterParserService.NOTICE_ISSUE_FILTER_ID: {
                return domainCrawlingInfo.getNotice();
            }
            default:
                return 0;
        }
    }

    private void save(Domain domain, FilterConfig filterConfig, long count) {
        if (count == 0) {
            return;
        }
        DomainFilter domainFilter = new DomainFilter();

        domainFilter.setCount((int) count);
        domainFilter.setDomainCrawlingInfoId(domain.getProcessDomainCrawlingInfo().getId());
        domainFilter.setFilterId(filterConfig.getId());
        domainFilter.setVersion(CommonVersion.NEW);

        log.info("{} count: {} {}", domain.getId(), count, filterConfig.title);

        domainFilterService.save(domainFilter);
    }

    public void fillIssueStatus(Domain domain) {
        String pageTableName = TableService.getFullTableName(domain.getId(), SchemaType.FINAL, TableType.PAGE);
        String nodeTableName = TableService.getFullTableName(domain.getId(), SchemaType.FINAL, TableType.NODE);

        FilterImportance.ISSUES_TYPES.forEach(importance -> {
            List<Integer> filters = FilterParserService.issueIdToFilterIds.get(importance);

            String importanceFilterIds = filters.stream().map(String::valueOf).collect(Collectors.joining(","));

            String template = "update %s p_update " +
                    "set filters = array_append(p_update.filters, %s) " +
                    "from %s p_select " +
                    "inner join %s n on n.id = p_select.id " +
                    "where p_select.id = p_update.id and n.type = %s and not (ARRAY [%s] :: int[] && p_select.filters) " +
                    "AND (ARRAY [%s] :: int[] && p_select.filters)";

            String sql = String.format(
                    template,
                    pageTableName, importance.getValue(), pageTableName,
                    nodeTableName, NodeType.INTERNAL.getValue(),
                    importance.getValue(), importanceFilterIds
            );
            queryExecutor.executeQuery(sql);
        });
    }
}