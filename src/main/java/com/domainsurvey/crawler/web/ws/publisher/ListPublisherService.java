package com.domainsurvey.crawler.web.ws.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.service.dao.painator.PaginatorProcessor;
import com.domainsurvey.crawler.service.dao.painator.model.PaginatorResult;
import com.domainsurvey.crawler.web.ws.model.request.message.DomainPaginationRequest;
import com.domainsurvey.crawler.web.ws.model.response.MessageList;

@Log4j2
@Service
@RequiredArgsConstructor
public class ListPublisherService extends AbstractPublisher {

    private final PaginatorProcessor paginatorProcessor;

    public void publish(DomainPaginationRequest domainPaginationRequest) throws Exception {
        log.info("requested {}", domainPaginationRequest);

        MessageList result = getResult(domainPaginationRequest);

        log.info("result size {} : {}", domainPaginationRequest.getMessageType(), result.getResult().getData().size());

        publishToChanel(result);
    }

    protected MessageList getResult(DomainPaginationRequest domainPaginationRequest) throws Exception {
        PaginatorResult result = paginatorProcessor.getPaginatorResult(domainPaginationRequest);

        return new MessageList(
                domainPaginationRequest.getId(),
                domainPaginationRequest.getMessageType(),
                result,
                domainPaginationRequest
        );
    }
}