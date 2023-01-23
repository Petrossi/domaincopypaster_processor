package com.domainsurvey.crawler.service.fetcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import static com.domainsurvey.crawler.service.fetcher.model.HttpConfig.DESCRIPTOR_METHOD;
import static com.domainsurvey.crawler.service.fetcher.model.HttpConfig.HTTP_METHOD;
import static com.domainsurvey.crawler.utils.Constants.BAD_STATUS_CODES;
import static com.domainsurvey.crawler.utils.UrlHelper.getValidUrl;
import static com.domainsurvey.crawler.utils.UrlHelper.isUrlValidForDomain;
import static com.domainsurvey.crawler.utils.Utils.getValidDomainUrl;
import static com.domainsurvey.crawler.utils.Utils.isRedirected;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.exception.DomainUrlNotValidException;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.service.fetcher.model.HttpConfig;
import com.domainsurvey.crawler.service.fetcher.model.HttpRequestInfo;
import com.domainsurvey.crawler.service.fetcher.model.HttpResult;

@Log4j2
@Service
@RequiredArgsConstructor
public class FetcherProcessor {

    private final HttpClientFetcher httpClientFetcher;
    private final PageFetcherService pageFetcherService;

    @Cacheable(value = "httpResult", keyGenerator = "httpConfigKeyGenerator")
    public HttpResult getPage(HttpConfig httpConfig) throws IOException {
        HttpResult httpResult = null;
        long start = System.currentTimeMillis();

        if (httpConfig.serviceType.equals(HTTP_METHOD)) {
            httpResult = httpClientFetcher.getPage(httpConfig);
        }

        if (httpResult == null) {
            httpConfig.serviceType = DESCRIPTOR_METHOD;
            return pageFetcherService.getPage(httpConfig);
        }

        httpResult.realTime = System.currentTimeMillis() - start;
        return httpResult;
    }

    public boolean processIsSpa(Domain domain) {
        boolean isSpa = false;

//        try {
//            isSpa = detectSpa(domain.getUrl());
//        } catch (IOException ignored) {
//        }

        return isSpa;
    }

    private boolean detectSpa(String url) throws IOException {
        HttpConfig httpConfig = HttpConfig.builder().url(url).serviceType(DESCRIPTOR_METHOD).build();
        HttpResult httpResult = getPage(httpConfig);
        boolean result = false;
        try {
            result = !httpResult.serviceType.equals("") && !httpResult.serviceType.equals("http");
        } catch (NullPointerException ignored) {
        }

        return result;
    }

    public String getValidDomainProtocol(String domainUrl) throws MalformedURLException, DomainUrlNotValidException {
        if (!domainUrl.startsWith("http")) {
            domainUrl = "https://" + domainUrl;
        }
        URL url = new URL(domainUrl);

        String host = url.getHost();
        String currentProtocol = url.getProtocol() + "://";
        String domainProtocol = currentProtocol + (host.startsWith("www") ? "www." : "");

        List<String> protocols = Stream.of(
                "https://www.",
                "https://",
                "http://www.",
                "http://"
        ).collect(Collectors.toList());

        if (!protocols.get(0).equals(domainProtocol)) {
            protocols = protocols.stream().filter(protocol -> !protocol.equals(domainProtocol)).collect(Collectors.toList());
            protocols.add(0, domainProtocol);
        }

        if (host.startsWith("www.")) {
            host = host.replace("www.", "");
        }
        String finalHost = host;
        List<String> urlsToCheck = protocols.stream().map(protocol -> protocol + finalHost).collect(Collectors.toList());

        List<String> logs = new ArrayList<>();
        for (String urlToCheck : urlsToCheck) {
            try {

                return checkValidUrl(urlToCheck, true, 1);
            } catch (DomainUrlNotValidException e) {
                logs.add(e.getMessage());
            }
        }

        throw new DomainUrlNotValidException(String.join(",", logs));
    }

    public String getValidFirstDomainUrlWithSlash(String domainUrl, boolean isDomain) throws MalformedURLException, DomainUrlNotValidException {
        if (!domainUrl.startsWith("http")) {
            domainUrl = "https://" + domainUrl;
        }
        URL url = new URL(domainUrl);

        String host = url.getHost();
        String currentProtocol = url.getProtocol() + "://";
        String domainProtocol = currentProtocol + (host.startsWith("www") ? "www." : "");

        host = (host.startsWith("www") ? host.replace("www.", "") : host);
        List<String> protocols = Stream.of(
                "https://www.",
                "https://",
                "http://www.",
                "http://"
        ).collect(Collectors.toList());

        if (!protocols.get(0).equals(domainProtocol)) {
            protocols = protocols.stream().filter(protocol -> !protocol.equals(domainProtocol)).collect(Collectors.toList());
            protocols.add(0, domainProtocol);
        }

        String finalHost = host;
        List<String> urlsToCheck = protocols.stream().map(protocol -> protocol + finalHost).collect(Collectors.toList());

        List<String> logs = new ArrayList<>();
        for (String urlToCheck : urlsToCheck) {
            urlToCheck = urlToCheck + "/";
            try {
                return checkValidUrl(urlToCheck, isDomain, 1);
            } catch (DomainUrlNotValidException e) {
                logs.add(e.getMessage());
            }
        }

        throw new DomainUrlNotValidException(String.join(",", logs));
    }

    public String checkValidUrl(String urlToCheck, boolean isDomain, int retryCount) throws DomainUrlNotValidException, MalformedURLException {
        log.info("trying check: {}", urlToCheck);

        if (retryCount == 5) {
            log.info("to many redirect: {}", urlToCheck);

            throw new DomainUrlNotValidException(urlToCheck + " to many redirect");
        }
        HttpRequestInfo httpRequestInfo = getHttpRequestInfo(urlToCheck, 2, 5);

        log.info("httpRequestInfo: {}", httpRequestInfo);

        if (httpRequestInfo.statusCode == 200) {
            return isDomain ? getValidDomainUrl(urlToCheck) : urlToCheck;
        }
        if (isRedirected(httpRequestInfo.statusCode)) {
            String redirectedUrl = getValidUrl(httpRequestInfo.getLocation(), urlToCheck);

            if (isUrlValidForDomain(urlToCheck, redirectedUrl)) {
                return checkValidUrl(redirectedUrl, isDomain, retryCount + 1);
            }
        }

        log.info("invalid status code: {} {}", urlToCheck, httpRequestInfo.statusCode);

        throw new DomainUrlNotValidException(urlToCheck + " urlToCheck: " + httpRequestInfo.statusCode);
    }


    public HttpRequestInfo getHttpRequestInfo(String url, int maxRetries, int timeout) {
        HttpRequestInfo httpStatusCodeAndContentType = new HttpRequestInfo();

        int count = 0;

        while (count < maxRetries) {
            HttpConfig config = HttpConfig.builder()
                    .url(url)
                    .proxy(count > 0)
                    .onlyHeaders(true)
                    .timeout(timeout)
                    .build();

            count++;
            try {
                HttpResult result = getPage(config);
                fillFromHtmlResult(result, httpStatusCodeAndContentType);
            } catch (Exception e) {
                continue;
            }

            if (BAD_STATUS_CODES.contains(httpStatusCodeAndContentType.statusCode)) {
                continue;
            }

            break;
        }

        return httpStatusCodeAndContentType;
    }

    private void fillFromHtmlResult(HttpResult httpResult, HttpRequestInfo httpStatusCodeAndContentType) {
        httpStatusCodeAndContentType.setStatusCode(httpResult.httpStatusCode);
        httpStatusCodeAndContentType.setContentType(httpResult.contentType);
        httpStatusCodeAndContentType.setContentLength(httpResult.contentLength);
        httpStatusCodeAndContentType.setProxy(httpResult.proxy);
        httpStatusCodeAndContentType.setLocation(httpResult.location);
        httpStatusCodeAndContentType.setHeaders(httpResult.headers);
        httpStatusCodeAndContentType.setContentLength(httpResult.contentLength);
    }
}