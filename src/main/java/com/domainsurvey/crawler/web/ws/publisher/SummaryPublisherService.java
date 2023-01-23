package com.domainsurvey.crawler.web.ws.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.domain.DomainCrawlingInfo;
import com.domainsurvey.crawler.model.filter.DomainFilter;
import com.domainsurvey.crawler.service.dao.DomainService;
import com.domainsurvey.crawler.service.dao.painator.FilterPaginator;
import com.domainsurvey.crawler.web.ws.model.response.MessageSummary;

@Log4j2
@Service
@RequiredArgsConstructor
public class SummaryPublisherService extends AbstractPublisher {

    private final DomainService domainService;
    private final FilterPaginator filterPaginator;

    public void publishSummary(String id) {
        log.info("ask summary for: {}", id);
        Optional<Domain> domainOptional = domainService.find(id);

        domainOptional.ifPresent((domain -> {
            DomainCrawlingInfo domainCrawlingInfo = domain.getFinalDomainCrawlingInfo();
            List<DomainFilter> domainFilters = filterPaginator.getFiltersByDomain(domain);

            MessageSummary messageSummary = new MessageSummary(id, domainCrawlingInfo, domainFilters);

            publishToChanel(messageSummary);
        }));
    }
}