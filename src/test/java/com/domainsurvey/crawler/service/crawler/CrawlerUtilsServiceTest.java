package com.domainsurvey.crawler.service.crawler;

import java.util.Collections;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import com.domainsurvey.crawler.Application;
import com.domainsurvey.crawler.exception.CantCreateCrawlingTablesException;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.page.Page;
import com.domainsurvey.crawler.service.crawler.importer.InsertImportService;
import com.domainsurvey.crawler.service.dao.DomainServiceTest;
import com.domainsurvey.crawler.service.dao.page.ProgressPageService;
import com.domainsurvey.crawler.service.table.TableService;
import com.domainsurvey.crawler.service.urlProcessor.model.SavedMetaData;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class CrawlerUtilsServiceTest {

    @Autowired
    private TableService tableService;

    @Autowired
    private ProgressPageService progressPageService;

    @Autowired
    private InsertImportService insertImportService;

    private Domain domain = null;
    private Page page = null;
    private SavedMetaData savedMetaData = SavedMetaData.
            builder().
            canonical("test").
            description("test").
            h1("test").
            title("test").
            build();

    @Before
    public void setUp() throws CantCreateCrawlingTablesException {
        domain = DomainServiceTest.buildTestDomain();

        tableService.createCrawlerProcessTables(domain);

        progressPageService.create(page, domain);

        page.setSavedMetaData(savedMetaData);

        insertImportService.importPages(Collections.singletonList(page), domain);

    }
}