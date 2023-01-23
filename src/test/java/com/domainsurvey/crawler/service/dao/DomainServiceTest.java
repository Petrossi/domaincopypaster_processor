package com.domainsurvey.crawler.service.dao;

import java.util.List;
import java.util.Optional;

import com.domainsurvey.crawler.Application;
import com.domainsurvey.crawler.model.domain.Config;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.type.CrawlingStatus;
import com.domainsurvey.crawler.service.crawler.DomainStorage;
import com.domainsurvey.crawler.service.dao.DomainService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class DomainServiceTest {

    @Autowired
    private DomainService domainService;

    @Autowired
    private DomainStorage domainStorage;

    @Test
    public void test_whenSaveAndRetrieveEntity_thenOK() {
        Domain domain = buildTestDomain();

        domainService.save(domain);

        Optional<Domain> foundEntity = domainService.find(domain.getId());

        Assert.assertNotEquals("Domain exists after save", foundEntity, Optional.empty());

        foundEntity.ifPresent(foundDomain -> {
            Assert.assertEquals("Domain id equals after save", domain.getId(), foundEntity.get().getId());
            Assert.assertNotNull("Domain config not null after save: ", domain.getConfig());
        });
    }

    public static Domain buildTestDomain() {
        Config config = Config.builder()
                .ignoreRobots(true)
                .pagesLimit(10)
                .spa(false)
                .threadCount(5)
                .robotsContent("User-Agent: *")
                .build();
        Domain domain = Domain.builder().host("test.com").protocol("http").config(config).build();
        domain.getConfig().setDomain(domain);

        return domain;
    }

    @Test
    public void test_getOneNewDomainForCrawling() {
        Domain domain = buildTestDomain();

        domainStorage.addToQueue(domain);

        Optional<Domain> domainForCrawling = domainService.getOneNewDomainForCrawling();

        Assert.assertTrue("Domain exists after save", domainForCrawling.isPresent());
    }

    @Test
    public void test_findByCrawlingStatus() {
        Domain domain = buildTestDomain();

        domainStorage.addToQueue(domain);

        List<Domain> domainForCrawling = domainService.findByCrawlingStatus(CrawlingStatus.QUEUE);

        Assert.assertFalse("Domain exists after save", domainForCrawling.isEmpty());
    }
}