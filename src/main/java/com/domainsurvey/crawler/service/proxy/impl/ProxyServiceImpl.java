package com.domainsurvey.crawler.service.proxy.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import static com.domainsurvey.crawler.service.fetcher.model.HttpConfig.HTTP_METHOD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import org.apache.http.HttpHost;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.service.fetcher.FetcherProcessor;
import com.domainsurvey.crawler.service.fetcher.model.HttpConfig;
import com.domainsurvey.crawler.service.proxy.ProxyService;
import com.domainsurvey.crawler.service.proxy.model.CustomHttpProxyInterface;
import com.domainsurvey.crawler.service.proxy.model.CustomProxy;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProxyServiceImpl implements ProxyService {

    private final BeanFactory beanFactory;

    private Random randomGenerator = new Random();

    @Value("crawler.proxy.url")
    private String url;

    @Value("#{new Boolean('${crawler.proxy.enabled}')}")
    private boolean enabled;

    private volatile boolean inited;

    private List<CustomProxy> proxies = Collections.synchronizedList(new ArrayList<>());

    @PostConstruct
    public void init() {
        initProxies();
    }

    @Scheduled(fixedDelayString = "${crawler.proxy.updateDelay}")
    public void initProxies() {
        try {
            if (!enabled) {
                return;
            }

            String content = beanFactory.getBean(FetcherProcessor.class).getPage(
                    HttpConfig
                            .builder()
                            .url(url)
                            .proxy(false)
                            .serviceType(HTTP_METHOD)
                            .build()
            ).html;

            stringToProxy(content);

            inited = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stringToProxy(String content) {
        proxies = Arrays.stream(content.split("\n")).filter(prxyStr -> !Objects.equals(prxyStr, ":")).map(proxyStr -> {
            try {
                String prosyHost = proxyStr.split(":")[0];
                byte proxyPort = Byte.parseByte(proxyStr.split(":")[1]);

                return new CustomProxy(prosyHost, proxyPort);
            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private boolean canReturnProxy() {
        return proxies.size() <= 0 || !enabled || !inited;
    }

    public CustomHttpProxyInterface getRandomProxy() {
        int proxyCount = proxies.size();
        if (canReturnProxy()) {
            return null;
        }

        int index = randomGenerator.nextInt(proxyCount);

        return proxies.get(index);
    }

    public HttpHost getApacheRandomProxy() {
        if (canReturnProxy()) {
            return null;
        }

        return getRandomProxy().toApacheProxy();
    }
}