package com.domainsurvey.crawler.service.backend;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.type.CrawlingStatus;
import com.domainsurvey.crawler.service.backend.model.BackendDomain;
import com.domainsurvey.crawler.service.crawler.DomainStorage;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.service.table.type.SchemaType;
import com.domainsurvey.crawler.service.table.type.TableType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import static com.domainsurvey.crawler.utils.Utils.sleepSeconds;

@Service
@RequiredArgsConstructor
@Log4j2
public class BackendService implements Runnable {

    private final QueryExecutor queryExecutor;
    private final DomainStorage domainStorage;

    private static final String tableName = String.format("%s.%s", SchemaType.BACKEND, TableType.DOMAIN);

    @Transactional
    public BackendDomain getNewDomain() throws EmptyResultDataAccessException {
        BackendDomain backendDomain;
        try {
            backendDomain = getByStatus(CrawlingStatus.CREATED);
        } catch (EmptyResultDataAccessException e) {
            backendDomain = getByStatus(CrawlingStatus.RE_CRAWLING);
        }

        updateDomainStatus(backendDomain.getId(), CrawlingStatus.QUEUE);

        return backendDomain;
    }

    @Transactional
    public BackendDomain getByStatus(CrawlingStatus crawlingStatus) throws EmptyResultDataAccessException {
        String sql = String.format(getSelectFrom() + " where status = %s limit 1", crawlingStatus);

        return getBySql(sql);
    }

    @Transactional
    public BackendDomain getDeletable() throws EmptyResultDataAccessException {
        String sql = String.format(getSelectFrom() + " where status = %s and non_deletable isnull limit 1", CrawlingStatus.DELETED);

        return getBySql(sql);
    }

    public String getSelectFrom() {
        return String.format("SELECT * from %s", tableName);
    }

    public BackendDomain getBySql(String sql) {
        return queryExecutor.queryForObject(sql, (rs, var1) -> {
            BackendDomain domain = new BackendDomain();
            domain.setId(rs.getString("id"));
            domain.setHost(rs.getString("host"));
            domain.setProtocol(rs.getString("protocol"));
            domain.setIgnoreRobots(rs.getBoolean("ignore_robots"));
            domain.setPagesLimit(rs.getInt("pages_limit"));
            domain.setThreadCount(rs.getByte("thread_count"));
            domain.setStatus(CrawlingStatus.fromValue(rs.getByte("status")));
            domain.setReportSaved(rs.getBoolean("report_saved"));

            return domain;
        });
    }

    public void updateDomainStatus(String id, CrawlingStatus crawlingStatus) {
        String sql = String.format("UPDATE %s set status = %s where id ='%s'", tableName, crawlingStatus, id);

        queryExecutor.executeQuery(sql);
    }

    public void start() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        log.info("start");
        while (true) {
            try {
                Domain domain = Domain.builder().backendDomain(getNewDomain()).build();

                domainStorage.addToQueue(domain);
                log.debug(String.format("new domain: %s %s detected", domain.getId(), domain.getUrl()));
            } catch (EmptyResultDataAccessException e) {
                log.debug(e.toString());
            }

            sleepSeconds(3);
        }
    }

    public void deleteById(String id) {
        log.info("deleteById: {}", id);

        String sql = String.format("DELETE FROM %s WHERE id = '%s'", tableName, id);

        queryExecutor.executeQuery(sql);
    }
}