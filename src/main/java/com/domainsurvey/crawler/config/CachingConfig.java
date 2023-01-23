package com.domainsurvey.crawler.config;

import java.util.HashMap;
import java.util.Map;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;
import com.domainsurvey.crawler.service.fetcher.model.HttpConfig;
import com.domainsurvey.crawler.utils.Utils;

@Configuration
@Profile("local")
@EnableCaching
public class CachingConfig {

    @Bean(destroyMethod = "shutdown")
    RedissonClient redisson() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379");
        return Redisson.create(config);
    }

    @Bean
    CacheManager cacheManager(RedissonClient redissonClient) {
        Map<String, CacheConfig> config = new HashMap<>();
        // create "testMap" spring cache with ttl = 24 minutes and maxIdleTime = 12 minutes
        config.put("testMap", new CacheConfig(24 * 60 * 1000, 12 * 60 * 1000));
        return new RedissonSpringCacheManager(redissonClient, config);
    }

    @Bean("httpConfigKeyGenerator")
    public KeyGenerator keyGenerator() {
        return (o, method, objects) -> {
            HttpConfig httpConfig = (HttpConfig) objects[0];

            return StringUtils.arrayToDelimitedString(new Object[]{
                    Utils.getCRC32(httpConfig.getUrl()),
                    httpConfig.getType(),
                    httpConfig.isOnlyHeaders(),
                    httpConfig.isFollowRedirect(),
                    httpConfig.isClearHtml(),
                    httpConfig.getContentType(),
                    httpConfig.isProxy(),
                    httpConfig.getRequestData() != null ? Utils.getCRC32(httpConfig.getRequestData()) : ""
            }, "_");
        };
    }
}