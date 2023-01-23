package com.domainsurvey.crawler.service.crawler.finalizer.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import static com.domainsurvey.crawler.utils.Utils.getCurrentTimestamp;
import static com.domainsurvey.crawler.utils.Utils.sleepSeconds;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.exception.CrawlingPageTableEmptyException;
import com.domainsurvey.crawler.exception.DomainQueueEmptyException;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.domain.DomainCrawlingInfo;
import com.domainsurvey.crawler.model.type.CrawlingPriority;
import com.domainsurvey.crawler.model.type.CrawlingStatus;
import com.domainsurvey.crawler.model.type.FinalizerStatus;
import com.domainsurvey.crawler.service.crawler.finalizer.CrawlingFinalizerProcessor;
import com.domainsurvey.crawler.service.crawler.finalizer.CrawlingFinalizerService;
import com.domainsurvey.crawler.service.crawler.finalizer.impl.processor.CountPageRankProgressFinalizerProcessor;
import com.domainsurvey.crawler.service.crawler.finalizer.impl.processor.FinalizingProgressProcessor;
import com.domainsurvey.crawler.service.crawler.finalizer.impl.processor.ImportIntoFinalTablesProgressProcessor;
import com.domainsurvey.crawler.service.crawler.finalizer.impl.processor.UpdateNodeCountProgressProcessor;
import com.domainsurvey.crawler.service.crawler.finalizer.impl.processor.UpdateRedirectedLinksProgressProcessor;
import com.domainsurvey.crawler.service.dao.DomainCrawlingInfoService;
import com.domainsurvey.crawler.service.dao.DomainService;
import com.domainsurvey.crawler.service.table.TableService;
import com.domainsurvey.crawler.thread.CrawlingFinalizerServiceThread;

@Service
@RequiredArgsConstructor
@Log4j2
public class CrawlingFinalizerServiceImpl implements CrawlingFinalizerService, Runnable {

    private final TableService tableService;
    private final DomainService domainService;
    private final DomainCrawlingInfoService domainCrawlingInfoService;
    private final BeanFactory beanFactory;

    private ExecutorService service = Executors.newFixedThreadPool(70);

    private final Object lock = new Object();

    private volatile boolean working;

    public void start() {
        if (working) {
            return;
        }

        init();
    }

    private void init() {
        working = true;

        log.info("init PageRankCounterService");

        domainService.findByCrawlingStatus(CrawlingStatus.WAITING_FINALIZE_IN_PROGRESS).forEach(this::finalize);

        new CrawlingFinalizerServiceThread(this).start();
    }

    public void stop() {
        working = false;
    }

    public void addToQueue(Domain domain) {
        log.info("domain.setIsCrawling(Domain.STATUS_WAITING_FOR_FINALIZE)");

        domain.setStatus(CrawlingStatus.WAITING_FOR_FINALIZE);

        domainService.save(domain);
    }

    public void saveDateFinalizerStarted(Domain domain) {
        DomainCrawlingInfo domainInfo = domain.getProcessDomainCrawlingInfo();

        domain.getProcessDomainCrawlingInfo().setFinalizerStartedTimestamp(getCurrentTimestamp());

        domainCrawlingInfoService.save(domainInfo);
    }

    public void finalizeProcess(Domain domain) throws CrawlingPageTableEmptyException {
        DomainCrawlingInfo domainInfo = domain.getProcessDomainCrawlingInfo();

        if (!domainInfo.getFinalizerStatus().equals(FinalizerStatus.FINALIZED)) {
            saveDateFinalizerStarted(domain);

            if (domainInfo.getTotal() == 0) {
                throw new CrawlingPageTableEmptyException(domain.getId(), "1");
            }

            if (domainInfo.getFinalizerStatus().equals(FinalizerStatus.NOT_COUNTING)) {
                domainInfo.setFinalizerStatus(FinalizerStatus.UPDATE_NODE_COUNT);
            }

            //TODO import by order
            Map<FinalizerStatus, CrawlingFinalizerProcessor> finalizers = new HashMap<FinalizerStatus, CrawlingFinalizerProcessor>() {{
                put(FinalizerStatus.UPDATE_NODE_COUNT, beanFactory.getBean(UpdateNodeCountProgressProcessor.class));
                put(FinalizerStatus.UPDATE_REDIRECTED_LINKS, beanFactory.getBean(UpdateRedirectedLinksProgressProcessor.class));
                put(FinalizerStatus.COUNT_PAGE_RANK, beanFactory.getBean(CountPageRankProgressFinalizerProcessor.class));
                put(FinalizerStatus.IMPORT_INTO_FINAL_TABLES, beanFactory.getBean(ImportIntoFinalTablesProgressProcessor.class));
                put(FinalizerStatus.FINALIZING, beanFactory.getBean(FinalizingProgressProcessor.class));
            }};

            while (!domainInfo.getFinalizerStatus().equals(FinalizerStatus.FINALIZED)) {
                CrawlingFinalizerProcessor finalizer = finalizers.get(domainInfo.getFinalizerStatus());

                try {
                    finalizer.process(domain);
                } catch (CrawlingPageTableEmptyException e) {
                    throw e;
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("{} {}", domain.getId(), e.getMessage());
                }
                FinalizerStatus finalizerStatus = finalizer.getNextStatus();
                domainInfo.setFinalizerStatus(finalizerStatus);

                domainCrawlingInfoService.save(domainInfo);
            }
        }
        log.info(
                "Finalizer ======================== {} total: {} | time: {}========================",
                domain.getId(), domainInfo.getTotal(), domainInfo.minutesFromStart()
        );
    }

    private void finalize(Domain nextDomainToCount) {
        service.submit(() -> {
            log.info("finalize start: " + nextDomainToCount.getId());

            try {
                finalizeProcess(nextDomainToCount);
            } catch (CrawlingPageTableEmptyException e) {
                hardRestartDomain(nextDomainToCount);
            } catch (Exception e) {
                saveErrorDomain(nextDomainToCount, e);
            }
        });
    }

    public void saveErrorDomain(Domain domain, Exception e) {
        e.printStackTrace();
        log.error(e.getMessage());
        domain.setStatus(CrawlingStatus.FINALIZING_FAILED);
        domainService.save(domain);
    }

    public void hardRestartDomain(Domain domain) {
        log.info("hardRestartDomain: " + domain.getId());
        if (domain.getProcessDomainCrawlingInfo().getHardRetryCount() > 3) {
            saveErrorDomain(domain, new Exception("too much retry"));
            return;
        }
        tableService.deleteAllCrawlingTables(domain.getId());
        domain.setPriority(CrawlingPriority.RESTART);
        domain.getProcessDomainCrawlingInfo().setHardRetryCount((byte) (domain.getProcessDomainCrawlingInfo().getHardRetryCount() + 1));
        domainService.addToCrawling(domain);
    }

    @Override
    public void finalizeDomainParsing(Domain domain) {
        beanFactory.getBean(FinalizingProgressProcessor.class).finalizeDomainParsing(domain);
    }

    private void getAndProcess() throws DomainQueueEmptyException {
        Domain nextDomainToCount = getNextDomain();

        nextDomainToCount.setStatus(CrawlingStatus.WAITING_FINALIZE_IN_PROGRESS);
        domainService.save(nextDomainToCount);

        finalize(nextDomainToCount);
    }

    private synchronized Domain getNextDomain() throws DomainQueueEmptyException {
        Domain domain;

        synchronized (lock) {
            Optional<Domain> optional = domainService.findFirstByCrawlingStatus(CrawlingStatus.WAITING_FOR_FINALIZE);

            if (!optional.isPresent()) {
                throw new DomainQueueEmptyException();
            }
            domain = optional.get();
        }

        return domain;
    }

    @Override
    public void run() {
        while (working) {
            try {
                getAndProcess();
            } catch (Exception e) {
                sleepSeconds(3);
            }
        }
    }
}