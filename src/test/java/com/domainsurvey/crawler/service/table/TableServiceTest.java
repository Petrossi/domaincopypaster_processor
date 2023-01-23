package com.domainsurvey.crawler.service.table;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import com.domainsurvey.crawler.Application;
import com.domainsurvey.crawler.dao.util.DomainIdGenerator;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.service.dao.DomainService;
import com.domainsurvey.crawler.service.dao.DomainServiceTest;
import com.domainsurvey.crawler.service.table.type.SchemaType;
import com.domainsurvey.crawler.service.table.type.TableType;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class TableServiceTest {

    @Autowired
    private TableService tableService;

    @Autowired
    private DomainService domainService;

    private Domain domain = null;

    //    @Before
    public void setUp() {
        domain = DomainServiceTest.buildTestDomain();

        domainService.save(domain);
    }

    @Test
    public void test_createPageTable() {
        String id = DomainIdGenerator.generateNewId();

        tableService.createPageTable(id);

        boolean tableExists = tableService.tableExists(id, SchemaType.PROCESS, TableType.PAGE);

        Assert.assertTrue("createPageTable", tableExists);
    }

    @Test
    public void test_moveFromSchemaToSchema() {
        String id = DomainIdGenerator.generateNewId();

        tableService.createPageTable(id);

        boolean tableExists = tableService.tableExists(id, SchemaType.PROCESS, TableType.PAGE);

        Assert.assertTrue("create table", tableExists);

        tableService.moveFromSchemaToSchema(id, SchemaType.PROCESS, SchemaType.FINAL, TableType.PAGE);

        boolean movedTableExists = tableService.tableExists(id, SchemaType.PROCESS, TableType.PAGE);

        Assert.assertFalse("move tab;e ", movedTableExists);

        boolean finalTableExists = tableService.tableExists(id, SchemaType.FINAL, TableType.PAGE);

        Assert.assertTrue("final table", finalTableExists);
    }
}