package com.domainsurvey.crawler.service.dao.page;

import com.domainsurvey.crawler.Application;
import com.domainsurvey.crawler.service.dao.page.ProgressPageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class ProgressPageTest {

    @Autowired
    private ProgressPageService progressPageService;

    @Test
    public void save_testImport() {

    }
}