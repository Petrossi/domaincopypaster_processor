package com.domainsurvey.crawler.service.crawler.queueMaker;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import com.domainsurvey.crawler.Application;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.page.Page;
import com.domainsurvey.crawler.service.dao.DomainServiceTest;
import com.domainsurvey.crawler.service.dao.page.ProgressPageService;
import com.domainsurvey.crawler.service.table.TableService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class QueueMakerFetcherServiceTest {

    @Autowired
    private TableService tableService;

    @Autowired
    private ProgressPageService progressPageService;

    @Autowired
    private QueueMakerFetcherService queueMakerFetcherService;

    private Domain domain = null;
    private Page page = null;

    @Before
    public void setUp() {
        domain = DomainServiceTest.buildTestDomain();
        page = new Page();
        tableService.createPageTable(domain);

        progressPageService.create(page, domain);
    }

}