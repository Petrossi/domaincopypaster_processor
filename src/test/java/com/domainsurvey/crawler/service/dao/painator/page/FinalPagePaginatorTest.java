package com.domainsurvey.crawler.service.dao.painator.page;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import com.domainsurvey.crawler.Application;
import com.domainsurvey.crawler.dto.PageData;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.page.Page;
import com.domainsurvey.crawler.service.crawler.importer.InsertImportService;
import com.domainsurvey.crawler.service.dao.DomainServiceTest;
import com.domainsurvey.crawler.service.dao.page.ProgressPageService;
import com.domainsurvey.crawler.service.dao.painator.model.PaginatorResult;
import com.domainsurvey.crawler.service.table.TableService;
import com.domainsurvey.crawler.service.urlProcessor.model.SavedMetaData;
import com.domainsurvey.crawler.web.ws.model.request.message.DomainPaginationRequest;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class FinalPagePaginatorTest {

    @Autowired
    private TableService tableService;

    @Autowired
    private ProgressPageService progressPageService;

    @Autowired
    private PagePaginator pagePaginator;

    @Autowired
    private InsertImportService insertImportService;

    private Domain domain = null;
    private SavedMetaData savedMetaData = SavedMetaData.builder().canonical("test").description("test").h1("test").title("test").build();

    @Before
    public void setUp() {
        domain = DomainServiceTest.buildTestDomain();
        Page page = new Page();

        tableService.createPageTable(domain);

        progressPageService.create(page, domain);

        page.setSavedMetaData(savedMetaData);

        insertImportService.importPages(Collections.singletonList(page), domain);
    }

    @Test
    public void test_list(){
        PaginatorResult<PageData> result = pagePaginator.list(domainPaginationRequest(domain));

        Assert.assertFalse(result.getData().isEmpty());
    }

    public static DomainPaginationRequest domainPaginationRequest(Domain domain){
        DomainPaginationRequest request = new DomainPaginationRequest();
        request.setId(domain.getId());
        request.setPage(1);
        request.setPageSize(10);
        return request;
    }
}