package com.domainsurvey.crawler.service.robots;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import com.domainsurvey.crawler.Application;
import com.domainsurvey.crawler.utils.robots.SimpleRobotRules;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class RobotsTxtParserServiceTest {

    @Autowired
    private BeanFactory beanFactory;

    @Test
    public void test_initRules_Empty(){
        RobotsTxtParserService robotsTxtParserService = beanFactory.getBean(RobotsTxtParserService.class);
        String robotsContent = "";

        robotsTxtParserService.initRules(robotsContent);;

        Assert.assertNotNull(robotsTxtParserService);
    }

    @Test
    public void test_initRules_Invalid(){
        RobotsTxtParserService robotsTxtParserService = beanFactory.getBean(RobotsTxtParserService.class);
        String robotsContent = "User-agent: *\n" +
                " $34comments\n" +
                "Disallow: /*?*\n" +
                "Disallow: /*?";

        robotsTxtParserService.initRules(robotsContent);;

        Assert.assertEquals(2, robotsTxtParserService.getRules().size());
    }

    @Test
    public void test_initRule_Allow() {
        RobotsTxtParserService robotsTxtParserService = beanFactory.getBean(RobotsTxtParserService.class);
        String robotsContent = "User-agent: *\n" +
                "Disallow: /test";

        robotsTxtParserService.initRules(robotsContent);
        ;

        SimpleRobotRules.RobotRule robotRule = robotsTxtParserService.isAllowed("http://test.com/test");

        Assert.assertFalse(robotRule == null || robotRule._allow);
    }
}