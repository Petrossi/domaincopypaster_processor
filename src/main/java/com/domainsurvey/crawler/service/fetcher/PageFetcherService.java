package com.domainsurvey.crawler.service.fetcher;

import lombok.RequiredArgsConstructor;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.service.fetcher.model.HttpConfig;
import com.domainsurvey.crawler.service.fetcher.model.HttpResult;
import com.domainsurvey.crawler.utils.JsonConverter;

@Service
@RequiredArgsConstructor
public class PageFetcherService {

    private final HttpClientFetcher httpClientFetcher;

    @Value("${crawler.pageFetcher.url}")
    private String pageFetcherUrl;

    HttpResult getPage(HttpConfig httpConfig) throws IOException {
        httpConfig.maxRetries = 2;
        String requestData = JsonConverter.convertToJson(httpConfig);
        HttpConfig pfHttpConfig = HttpConfig.builder()
                .url(pageFetcherUrl + "/getPage")
                .type(HttpConfig.POST_REQUEST)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .requestData(requestData)
                .build();

        HttpResult response = httpClientFetcher.doPost(pfHttpConfig);
        return JsonConverter.convertFromJson(response.html, HttpResult.class);
    }
}