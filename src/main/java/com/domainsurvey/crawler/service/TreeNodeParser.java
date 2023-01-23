package com.domainsurvey.crawler.service;

import lombok.RequiredArgsConstructor;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import com.domainsurvey.crawler.service.crawler.content.model.TreeNode;
import com.domainsurvey.crawler.service.fetcher.FetcherProcessor;
import com.domainsurvey.crawler.service.fetcher.model.HttpConfig;
import com.domainsurvey.crawler.service.fetcher.model.HttpResult;

@Component
@RequiredArgsConstructor
public class TreeNodeParser {

    private final FetcherProcessor fetcherProcessor;

    public TreeNode parseHtmlContentByUrl(String url) throws IOException {
        HttpConfig config = HttpConfig.builder().url(url).timeout(5).followRedirect(true).build();

        HttpResult result = fetcherProcessor.getPage(config);

        return new TreeNode(Jsoup.parse(result.html).body());
    }
}    
