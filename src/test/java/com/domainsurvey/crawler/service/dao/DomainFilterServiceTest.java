package com.domainsurvey.crawler.service.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import com.domainsurvey.crawler.Application;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.domain.DomainCrawlingInfo;
import com.domainsurvey.crawler.model.filter.DomainFilter;
import com.domainsurvey.crawler.model.filter.FilterConfig;
import com.domainsurvey.crawler.model.type.CommonVersion;
import com.domainsurvey.crawler.service.filter.FilterParserService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class DomainFilterServiceTest {

    @Autowired
    private DomainFilterService domainFilterService;

    @Autowired
    private DomainCrawlingInfoService domainCrawlingInfoService;

    @Autowired
    private DomainService domainService;

    DomainCrawlingInfo domainCrawlingInfo = null;
    Domain domain;

    @Before
    public void setUp() {
        domain = DomainServiceTest.buildTestDomain();

        domainCrawlingInfo = DomainCrawlingInfo.builder().domain(domain).build();

        domain.setProcessDomainCrawlingInfo(domainCrawlingInfo);

        domainService.save(domain);
    }

    @Test
    public void test_createForEachFiltersNew() {
        Domain domain = domainService.find(this.domain.getId()).get();

        DomainCrawlingInfo domainCrawlingInfo = domain.getProcessDomainCrawlingInfo();

        List<FilterConfig> filters = FilterParserService.allFiltersList;

        List<DomainFilter> domainFilters = filters.stream().map(filter -> DomainFilter.builder()
                .domainCrawlingInfoId(domainCrawlingInfo.getId())
                .count(10)
                .filterId(filter.getId())
                .version(CommonVersion.NEW)
                .build()).collect(Collectors.toList());

        domainFilterService.saveAll(domainFilters);

        List<DomainFilter> savedDomainFilters = domainFilterService.findByDomainCrawlingInfo(domainCrawlingInfo);

        Assert.assertEquals("Filters compare ", domainFilters.size(), savedDomainFilters.size());
        Assert.assertEquals("Filters compare saved ", domainFilters.size(), savedDomainFilters.size());
    }
}