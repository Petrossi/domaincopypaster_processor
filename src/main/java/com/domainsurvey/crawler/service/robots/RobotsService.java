package com.domainsurvey.crawler.service.robots;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.exception.FailedToGetRobotsException;
import com.domainsurvey.crawler.model.domain.Domain;

@Service
@RequiredArgsConstructor
public class RobotsService{

    private final BeanFactory beanFactory;

    public RobotsTxtParserService createRobotsTxtParserServiceByDomain(Domain domain) throws FailedToGetRobotsException {
        RobotsTxtParserService robotsTxtParserService = null;
        if (!domain.getConfig().isIgnoreRobots() && domain.getConfig().getRobotsContent() != null && !domain.getConfig().getRobotsContent().isEmpty()) {
            robotsTxtParserService = beanFactory.getBean(RobotsTxtParserService.class, domain.getConfig().getRobotsContent());
        } else if (domain.getConfig().isIgnoreRobots()) {
            robotsTxtParserService = beanFactory.getBean(RobotsTxtParserService.class);

            robotsTxtParserService.initForUrl(domain.getUrl());
        }

        return robotsTxtParserService;
    }
}