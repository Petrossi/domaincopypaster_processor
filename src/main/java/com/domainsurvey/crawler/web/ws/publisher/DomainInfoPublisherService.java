package com.domainsurvey.crawler.web.ws.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.service.backend.PublicService;
import com.domainsurvey.crawler.service.backend.model.BackendDomain;
import com.domainsurvey.crawler.web.ws.model.response.MessageDomainInfo;

@Log4j2
@Service
@RequiredArgsConstructor
public class DomainInfoPublisherService extends AbstractPublisher {

    private final PublicService backendService;

    public void publishSummary(String id) {
        log.info("ask summary for: {}", id);

        String sql = String.format(backendService.getSelectFrom() + " where id = '%s' limit 1", id);

        BackendDomain domain = null;

        try {
            domain = backendService.getBySql(sql);
        } catch (Exception ignored) {
        }

        MessageDomainInfo messageSummary = new MessageDomainInfo(id, domain);

        publishToChanel(messageSummary);
    }
}