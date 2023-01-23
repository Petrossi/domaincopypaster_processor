package com.domainsurvey.crawler.service.crawler.finalizer.impl;

import lombok.SneakyThrows;

import java.util.Optional;

import com.domainsurvey.crawler.Application;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.domain.DomainCrawlingInfo;
import com.domainsurvey.crawler.service.crawler.finalizer.CrawlingFinalizerService;
import com.domainsurvey.crawler.service.dao.DomainService;
import com.domainsurvey.crawler.service.dao.DomainServiceTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.Assert;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class CrawlingFinalizerServiceImplTest {

    @Autowired
    private DomainService domainService;

    @Autowired
    private CrawlingFinalizerService crawlingFinalizerService;

    private Domain domain = null;

    @SneakyThrows
    @Before
    public void setUp() {
        domain = DomainServiceTest.buildTestDomain();
        DomainCrawlingInfo domainCrawlingInfo = DomainCrawlingInfo.builder().build();

        domain.setProcessDomainCrawlingInfo(domainCrawlingInfo);
        domainService.save(domain);
    }

    @Test
    public void test_saveDateFinalizerStarted_thenOk() {
        Optional<Domain> saved = domainService.find(domain.getId());

        saved.ifPresent(savedDomain -> {
            crawlingFinalizerService.saveDateFinalizerStarted(savedDomain);

            DomainCrawlingInfo saveDomainCrawlingInfo = savedDomain.getProcessDomainCrawlingInfo();

            Assert.assertNotNull("getFinalizerStartedTimestamp not null", saveDomainCrawlingInfo.getFinalizerStartedTimestamp());
        });
    }
}