package com.domainsurvey.crawler.web.ws;

import lombok.RequiredArgsConstructor;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import com.domainsurvey.crawler.web.ws.model.request.message.DomainPaginationRequest;
import com.domainsurvey.crawler.web.ws.publisher.DomainInfoPublisherService;
import com.domainsurvey.crawler.web.ws.publisher.ListPublisherService;
import com.domainsurvey.crawler.web.ws.publisher.PagePublisherService;
import com.domainsurvey.crawler.web.ws.publisher.SummaryPublisherService;

@Controller
@RequiredArgsConstructor
public class DataController {

    private final ListPublisherService listPublisherService;
    private final SummaryPublisherService summaryPublisherService;
    private final DomainInfoPublisherService domainInfoPublisherService;
    private final PagePublisherService pagePublisherService;

    @MessageMapping("/list")
    public void list(@Payload DomainPaginationRequest request) throws Exception {
        listPublisherService.publish(request);
    }

    @MessageMapping("/export")
    public void export(@Payload DomainPaginationRequest request) throws Exception {
        listPublisherService.publish(request);
    }

    @MessageMapping("/summary/{id}")
    public void summary(@DestinationVariable String id) {
        summaryPublisherService.publishSummary(id);
    }

    @MessageMapping("/info/{id}")
    public void info(@DestinationVariable String id) {
        domainInfoPublisherService.publishSummary(id);
    }

    @MessageMapping("/{domainId}/page/info/{pageId}")
    public void pageInfo(@DestinationVariable String domainId, @DestinationVariable long pageId) {
        pagePublisherService.publishInfo(domainId, pageId);
    }
}