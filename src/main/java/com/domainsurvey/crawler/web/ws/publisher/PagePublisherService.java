package com.domainsurvey.crawler.web.ws.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Collections;

import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.dto.PageData;
import com.domainsurvey.crawler.service.dao.painator.page.PagePaginator;
import com.domainsurvey.crawler.web.ws.model.request.message.DomainPaginationRequest;
import com.domainsurvey.crawler.web.ws.model.response.MessagePageInfo;

@Log4j2
@Service
@RequiredArgsConstructor
public class PagePublisherService extends AbstractPublisher {

    private final PagePaginator pagePaginator;

    public void publishInfo(String domainId, long pageId) {
        log.info("ask page info for: {} {} ", domainId, pageId);

        DomainPaginationRequest domainPaginationRequest = new DomainPaginationRequest();
        domainPaginationRequest.setId(domainId);
        domainPaginationRequest.setAdditionalData(Collections.singletonMap("pageId", String.valueOf(pageId)));

        PageData pageData = pagePaginator.list(domainPaginationRequest).getData().get(0);

        MessagePageInfo messageDomainInfo = new MessagePageInfo(domainId, pageData);

        publishToChanel(messageDomainInfo);
    }
}