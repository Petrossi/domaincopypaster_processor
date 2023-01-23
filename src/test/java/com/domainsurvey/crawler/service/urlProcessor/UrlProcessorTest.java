package com.domainsurvey.crawler.service.urlProcessor;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import com.domainsurvey.crawler.Application;
import com.domainsurvey.crawler.service.fetcher.FetcherProcessor;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class UrlProcessorTest {

    @Autowired
    private CrawlerResultProcessor crawlerResultProcessor;

    @Autowired
    private FetcherProcessor fetcherProcessor;
}