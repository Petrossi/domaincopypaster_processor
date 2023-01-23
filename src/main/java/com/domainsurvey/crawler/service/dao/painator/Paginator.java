package com.domainsurvey.crawler.service.dao.painator;

import com.domainsurvey.crawler.service.dao.painator.model.PaginatorResult;
import com.domainsurvey.crawler.web.ws.model.request.message.DomainPaginationRequest;

public interface Paginator<T> {
    PaginatorResult<T> list(DomainPaginationRequest domainPaginationRequest) throws Exception;
}
