package com.domainsurvey.crawler.service.dao.painator;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.service.dao.painator.link.ExternalLinkPaginator;
import com.domainsurvey.crawler.service.dao.painator.link.IncomingLinkPaginator;
import com.domainsurvey.crawler.service.dao.painator.link.InternalLinkPaginator;
import com.domainsurvey.crawler.service.dao.painator.model.PaginatorResult;
import com.domainsurvey.crawler.service.dao.painator.page.PagePaginator;
import com.domainsurvey.crawler.web.ws.model.request.message.DomainPaginationRequest;
import com.domainsurvey.crawler.web.ws.model.type.MessageType;

@Service
@RequiredArgsConstructor
public class PaginatorProcessor {

    private final ExternalLinkPaginator externalLinkPaginator;
    private final IncomingLinkPaginator incomingLinkPaginator;
    private final InternalLinkPaginator internalLinkPaginator;
    private final PagePaginator finalPagePaginator;
    private final FilterPaginator filterPaginator;

    public PaginatorResult getPaginatorResult(DomainPaginationRequest domainPaginationRequest) throws Exception {
        Paginator paginator = paginator(domainPaginationRequest.getMessageType());

        return paginator.list(domainPaginationRequest);
    }

    private Paginator paginator(MessageType messageType) throws Exception {
        switch (messageType) {
            case PAGE_EXPORT:
            case PAGE_LIST:
                return finalPagePaginator;
            case FILTER_LIST:
                return filterPaginator;
            case INTERNAL_LIST:
                return internalLinkPaginator;
            case INCOMING_LIST:
                return incomingLinkPaginator;
            case EXTERNAL_LIST:
                return externalLinkPaginator;
        }

        throw new Exception();
    }
}