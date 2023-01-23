package com.domainsurvey.crawler.service.crawler;

import lombok.SneakyThrows;

import java.util.Optional;

import com.domainsurvey.crawler.Application;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.service.dao.DomainService;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.service.dao.DomainServiceTest;
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
public class DomainStorageTest {

    @Autowired
    private DomainStorage domainStorage;
    @Autowired
    private DomainService domainService;
    @Autowired
    private QueryExecutor queryExecutor;

    @Before
    public void setUp() {

    }

    @Test
    public void test_addToQueueAndFindDomain() {
        Domain domain = DomainServiceTest.buildTestDomain();

        domainStorage.addToQueue(domain);

        Optional<Domain> savedDomain = domainService.find(domain.getId());

        Assert.assertTrue("Domain exists after save", savedDomain.isPresent());

        savedDomain.ifPresent(foundDomain -> {
            Assert.assertEquals("Domain id equals after save", domain.getId(), foundDomain.getId());
            Assert.assertEquals("Domain status queue after save", domain.getStatus(), foundDomain.getStatus());
            Assert.assertNotNull("Domain config not null after save: ", domain.getConfig());
            Assert.assertNotNull("Domain crawling info not null after save: ", domain.getProcessDomainCrawlingInfo());
        });
    }

    @SneakyThrows
    @Test
    public void test_getNextDomain() {
        queryExecutor.executeQuery("delete from domain");

        Domain domain = DomainServiceTest.buildTestDomain();
        domainStorage.addToQueue(domain);

        Domain domainToCrawl = domainStorage.getNextDomain();

        Assert.assertNotNull("Domain exists after save", domainToCrawl);

    }
}