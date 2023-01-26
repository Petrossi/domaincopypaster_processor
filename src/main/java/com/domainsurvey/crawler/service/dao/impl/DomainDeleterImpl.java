package com.domainsurvey.crawler.service.dao.impl;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.service.backend.PublicService;
import com.domainsurvey.crawler.service.backend.model.BackendDomain;
import com.domainsurvey.crawler.service.dao.DomainDeleter;
import com.domainsurvey.crawler.service.dao.DomainService;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.service.table.TableService;
import com.domainsurvey.crawler.thread.DeleteDeletedThread;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.domainsurvey.crawler.utils.Utils.sleepSeconds;

@Log4j2
@Service
@RequiredArgsConstructor
public class DomainDeleterImpl implements DomainDeleter {

    private final PublicService backendService;
    private final DomainService domainService;
    private final TableService tableService;
    private final QueryExecutor queryExecutor;


    public void start() {
        new DeleteDeletedThread(() -> {
            while (true) {
                try {
                    BackendDomain domain = backendService.getDeletable();

                    delete(domain.getId());
                    sleepSeconds(5);
                } catch (org.springframework.dao.EmptyResultDataAccessException e) {
                    Optional<Domain> domain = domainService.findDeleted();

                    domain.ifPresent(value -> delete(value.getId()));

                    sleepSeconds(5);

                    setDeleted();
                } catch (Throwable e) {
                    e.printStackTrace();
                    sleepSeconds(5);
                }
            }
        }).start();
    }

    private void setDeleted() {
        String sql = "update public.domain \n" +
                "set status = 7 \n" +
                "where non_deletable isnull and status = 2 and domain.created_at < (now() - '7 days'::interval);";
        try {
            this.queryExecutor.executeQuery(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete(String id) {
        log.info("delete: " + id);

        try {
            tableService.deleteCrawlingTables(id);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            tableService.deleteFinalTables(id);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            tableService.deleteLastCrawlingTables(id);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            backendService.deleteById(id);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            domainService.deleteById(id);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}