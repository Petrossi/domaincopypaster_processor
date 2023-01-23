package com.domainsurvey.crawler.service.robots;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import static com.domainsurvey.crawler.utils.Constants.BAD_STATUS_CODES;
import static java.nio.charset.StandardCharsets.UTF_8;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.exception.BadHttpStatusCodeException;
import com.domainsurvey.crawler.exception.FailedToGetRobotsException;
import com.domainsurvey.crawler.service.fetcher.FetcherProcessor;
import com.domainsurvey.crawler.service.fetcher.model.HttpConfig;
import com.domainsurvey.crawler.service.fetcher.model.HttpResult;
import com.domainsurvey.crawler.utils.robots.BaseRobotRules;
import com.domainsurvey.crawler.utils.robots.SimpleRobotRules;
import com.domainsurvey.crawler.utils.robots.SimpleRobotRulesParser;

@Service
@Scope("prototype")
@NoArgsConstructor
@Log4j2
public class RobotsTxtParserService {

    private String domainUrl;
    private BaseRobotRules rules;

    @Autowired
    private FetcherProcessor fetcherProcessor;

    private boolean inited;

    public RobotsTxtParserService(String robotsContent) {
        initRules(robotsContent);
    }

    public void initRules(String robotsContent) {
        rules = createRobotRules(robotsContent.getBytes(UTF_8));
        inited = true;
    }

    public void initForUrl(String domainUrl) throws FailedToGetRobotsException {
        this.domainUrl = domainUrl;
        String robotsUrl = domainUrl + "/robots.txt";
        HttpConfig httpConfig = HttpConfig.builder().url(robotsUrl).retryAtBadStatusCode(true).maxRetries(5).build();

        HttpResult httpResult;
        try {
            httpResult = fetcherProcessor.getPage(httpConfig);
        } catch (Exception e) {
            if (e instanceof BadHttpStatusCodeException) {
                log.error("robots: {} failed to get with statusCode: {}", robotsUrl, ((BadHttpStatusCodeException) e).statusCode);
            } else {
                log.error("robots: {} failed to get with message: {}", robotsUrl, e.getMessage());
            }

            throw new FailedToGetRobotsException();
        }

        short statusCode = httpResult.httpStatusCode;

        if (BAD_STATUS_CODES.contains(statusCode)) {
            log.error("robots: {} failed to get with statusCode: {}", robotsUrl, statusCode);
        }

        String contentType = httpResult.contentType;

        if (!contentType.equalsIgnoreCase("text/plain")) {
            log.error("robots: {} failed to get with contentType: {}", robotsUrl, contentType);
            throw new FailedToGetRobotsException();
        }

        initRules(httpResult.html);
    }

    private BaseRobotRules createRobotRules(byte[] content) {
        SimpleRobotRulesParser robotParser = new SimpleRobotRulesParser();

        return robotParser.parseContent(domainUrl, content, "text/plain", "Googlebot");
    }

    public BaseRobotRules getRules() {
        return rules;
    }

    public SimpleRobotRules.RobotRule isAllowed(String url) {
        if (!inited) {
            return null;
        }

        return rules.isAllowed(url);
    }
}
