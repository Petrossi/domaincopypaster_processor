package com.domainsurvey.crawler.service.backend;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import com.domainsurvey.crawler.Application;
import com.domainsurvey.crawler.service.backend.model.BackendDomain;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.service.table.type.SchemaType;
import com.domainsurvey.crawler.service.table.type.TableType;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class BackendServiceTest {

    @Autowired
    private QueryExecutor queryExecutor;

    @Autowired
    private BackendService backendService;

    private String tableName = String.format("%s.%s", SchemaType.BACKEND, TableType.DOMAIN);

    @Before
    public void setUp() {

        queryExecutor.executeQuery(String.format("delete from %s", tableName));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void whenExceptionThrown_thenExpectationSatisfied() {
        backendService.getNewDomain();
    }

    @Test
    public void test_getNewDomain() {
        queryExecutor.executeQuery(String.format(
                "insert into %s(id, host, protocol, status, ignore_robots, pages_limit, thread_count)  " +
                        "values ('test', 'test.com', 'http', 9, false, 2000, 5);", tableName
        ));

        BackendDomain notEmptyDomain = backendService.getNewDomain();

        Assert.assertNotNull(notEmptyDomain);
    }
}