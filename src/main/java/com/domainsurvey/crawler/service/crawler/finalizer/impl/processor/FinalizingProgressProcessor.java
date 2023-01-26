package com.domainsurvey.crawler.service.crawler.finalizer.impl.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import static com.domainsurvey.crawler.utils.Utils.getCurrentTimestamp;

import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.domain.DomainCrawlingInfo;
import com.domainsurvey.crawler.model.type.CrawlingPriority;
import com.domainsurvey.crawler.model.type.CrawlingStatus;
import com.domainsurvey.crawler.model.type.FinalizerStatus;
import com.domainsurvey.crawler.service.backend.PublicService;
import com.domainsurvey.crawler.service.crawler.finalizer.CrawlingFinalizerProcessor;
import com.domainsurvey.crawler.service.crawler.finalizer.impl.ColumnFilterService;
import com.domainsurvey.crawler.service.dao.DomainCrawlingInfoService;
import com.domainsurvey.crawler.service.dao.DomainService;
import com.domainsurvey.crawler.service.filter.FillFiltersService;
import com.domainsurvey.crawler.service.table.TableService;
import com.domainsurvey.crawler.web.ws.publisher.ProgressPublisherService;

@Service
@RequiredArgsConstructor
@Log4j2
public class FinalizingProgressProcessor implements CrawlingFinalizerProcessor {

    private final TableService tableService;
    private final ProgressPublisherService progressPublisherService;
    private final ColumnFilterService columnFilterService;
    private final FillFiltersService fillFiltersService;
    private final DomainService domainService;
    private final DomainCrawlingInfoService domainCrawlingInfoService;
    private final PublicService backendService;

    public void process(Domain domain) {
        log.info("start: " + domain.getId());

        finalizeDomainParsing(domain);

        log.info("finished : " + domain.getId());
    }

    public void finalizeDomainParsing(Domain domain) {
        processFilters(domain);

        domain.getProcessDomainCrawlingInfo().setFinalizerFinishedTimestamp(getCurrentTimestamp());

        finishDomain(domain);
    }

    public void processFilters(Domain domain) {
        columnFilterService.process(domain);
        fillFiltersService.process(domain);
        countScoreAsync(domain);
    }

    public void countScoreAsync(Domain domain) {
        log.info("countScoreByFilterAsync: " + domain.getId());

        domain.setScore(domain.getProcessDomainCrawlingInfo().getScore());

        log.info("finished countScoreByFilterAsync" + domain.getId());
    }

    public void finishDomain(Domain domain) {
        domain.setStatus(CrawlingStatus.FINISHED);
        domain.setCrawlCount(domain.getCrawlCount() + 1);
        domain.setPriority(CrawlingPriority.MONITORING);
        domain.getProcessDomainCrawlingInfo().setFinalizerStatus(FinalizerStatus.NOT_COUNTING);

        DomainCrawlingInfo domainInfo = domain.getProcessDomainCrawlingInfo();

        domainInfo.setScore(domain.getScore());
        domainInfo.setFinalizerFinishedTimestamp(getCurrentTimestamp());

        domainCrawlingInfoService.save(domainInfo);

        tableService.deleteAllCrawlingTables(domain.getId());

        domain.setFinalDomainCrawlingInfo(domainInfo);
        domain.setProcessDomainCrawlingInfo(null);

        domainService.save(domain);
        backendService.updateDomainStatus(domain.getId(), CrawlingStatus.FINISHED);
        progressPublisherService.publishFinish(domain.getId());
    }

    @Override
    public FinalizerStatus getNextStatus() {
        return FinalizerStatus.FINALIZED;
    }
}