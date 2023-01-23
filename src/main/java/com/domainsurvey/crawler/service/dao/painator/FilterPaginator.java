package com.domainsurvey.crawler.service.dao.painator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.filter.DomainFilter;
import com.domainsurvey.crawler.service.dao.DomainService;
import com.domainsurvey.crawler.service.dao.painator.model.PaginatorResult;
import com.domainsurvey.crawler.service.dao.repository.DomainFilterRepository;
import com.domainsurvey.crawler.web.ws.model.request.message.DomainPaginationRequest;

@Component
public class FilterPaginator implements Paginator<DomainFilter> {

    @Autowired
    private DomainFilterRepository domainFilterRepository;

    @Autowired
    private DomainService domainService;

    @Override
    public PaginatorResult<DomainFilter> list(DomainPaginationRequest domainPaginationRequest) {
        Optional<Domain> domain = domainService.find(domainPaginationRequest.getId());

        List<DomainFilter> list = new ArrayList<>();

        if (domain.isPresent()) {
            list = getFiltersByDomain(domain.get());
        }

        return new PaginatorResult<>(list, list.size(), list.size());
    }

    public List<DomainFilter> getFiltersByDomain(Domain domain) {
        return domainFilterRepository.findAllByDomainCrawlingInfoId(domain.getFinalDomainCrawlingInfo().getId());
    }
}