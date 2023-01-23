package com.domainsurvey.crawler.service.dao;

import java.util.Optional;

import com.domainsurvey.crawler.Application;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.domain.DomainCrawlingInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class DomainCrawlingInfoServiceTest {

    @Autowired
    private DomainCrawlingInfoService domainCrawlingInfoService;

    @Autowired
    private DomainService domainService;

    private Domain domain;
    private DomainCrawlingInfo domainInfo;

    @Before
    public void setUp() {
        domainInfo = buildTestProgressInfo();
        domain = domainInfo.getDomain();

//        domainCrawlingInfoService.save(domainInfo);
        domain.setProcessDomainCrawlingInfo(domainInfo);

        domainService.save(domain);
    }

    @Test
    public void test_whenSaveAndRetrieveEntity_change() {
        domainService.find(domain.getId()).ifPresent(domain -> {

            domainInfo = domain.getProcessDomainCrawlingInfo();

            domainInfo.setTotal(10);

            domainCrawlingInfoService.save(domainInfo);

            Optional<DomainCrawlingInfo> changedFromDb = domainCrawlingInfoService.find(domainInfo.getId());

            Assert.assertTrue("ProgressInfo exists after save", changedFromDb.isPresent());

            changedFromDb.ifPresent(c -> {
                Assert.assertEquals("ProgressInfo changed total save", c.getTotal(), 10L);
            });
        });
    }

    @Test
    public void test_whenSaveAndRetrieveEntity_() {
        domain = domainService.find(domain.getId()).get();

        domainInfo = domain.getProcessDomainCrawlingInfo();

        Optional<DomainCrawlingInfo> foundEntity = domainCrawlingInfoService.find(domainInfo.getId());

        Assert.assertTrue("ProgressInfo exists after save", foundEntity.isPresent());

        foundEntity.ifPresent(foundInfo -> {
            Domain foundInfoDomain = foundInfo.getDomain();

            Assert.assertEquals("ProgressInfo equals after save", domainInfo, foundInfo);
            Assert.assertEquals("ProgressInfo domain  equals after save", domainInfo.getDomain(), foundInfoDomain);
            Assert.assertNotNull("ProgressInfo finalizer status exists after save", domainInfo.getFinalizerStatus());
            Assert.assertNotNull("ProgressInfo queue added timestamp not null after save", domainInfo.getQueueAddedTimestamp());

            Optional<Domain> foundDomain = domainService.find(domain.getId());
            Assert.assertNotEquals("Domain exists after save", foundDomain, Optional.empty());
            foundDomain.ifPresent(d -> {
                Assert.assertEquals("Domain equals after save", d, domain);
                Assert.assertEquals("Domain ProgressInfo equals after save", d.getProcessDomainCrawlingInfo(), foundInfo);

                DomainCrawlingInfo process = DomainCrawlingInfo.builder().domain(d).build();

                d.setFinalDomainCrawlingInfo(d.getProcessDomainCrawlingInfo());
                d.setProcessDomainCrawlingInfo(process);
                domainService.save(d);

                Optional<Domain> foundDomainWithTwoInfo = domainService.find(domain.getId());
                Assert.assertNotEquals("foundDomainWithTwoInfo exists after save", foundDomainWithTwoInfo, Optional.empty());

                foundDomainWithTwoInfo.ifPresent(foundWithTwoInfo -> {
                    Assert.assertEquals("foundDomainWithTwoInfo equals after save", foundWithTwoInfo, domain);
                    Assert.assertNotNull("foundDomainWithTwoInfo progress exists after save", d.getProcessDomainCrawlingInfo());
                    Assert.assertNotNull("foundDomainWithTwoInfo final exists after save", d.getFinalDomainCrawlingInfo());
                });
            });
        });
    }

    public static DomainCrawlingInfo buildTestProgressInfo() {
        Domain domain = DomainServiceTest.buildTestDomain();

        return DomainCrawlingInfo.builder().domain(domain).build();
    }
}