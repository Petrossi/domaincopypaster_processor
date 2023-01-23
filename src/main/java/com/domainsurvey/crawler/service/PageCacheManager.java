package com.domainsurvey.crawler.service;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.service.fetcher.model.HttpResult;
import com.domainsurvey.crawler.utils.FileUtils;
import com.domainsurvey.crawler.utils.JsonConverter;
import com.domainsurvey.crawler.utils.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.domainsurvey.crawler.utils.FileUtils.isDirectoryExists;
import static com.domainsurvey.crawler.utils.FileUtils.readFileAsString;
import static com.domainsurvey.crawler.utils.FileUtils.writeToFile;

@Service
public class PageCacheManager {

    @Value( "${crawler.page_cache.directory}" )
    protected String directory;

    @Value( "${crawler.page_cache.enabled}" )
    protected boolean enabled;

    public void deleteCache(Domain domain){
        try{
            String path = getFullDirectory(Utils.getCRC32(domain.getUrl()));

            if(isDirectoryExists(path)){
                FileUtils.deleteDirectory(path);
            }
        }catch (Exception ignored){ }
    }

    public void savePage(long crc32DomainUrl, HttpResult httpResult, long crc32){
        if(!enabled){
            return;
        }
        String fileName = getFullPathForUrl(crc32DomainUrl, crc32);

        writeToFile(fileName, JsonConverter.convertToJson(httpResult));
    }

    public HttpResult getPage(long crc32DomainUrl, long crc32) throws Exception {
        String fileName = getFullPathForUrl(crc32DomainUrl, crc32);

        String json = readFileAsString(fileName);

        if(json.isEmpty()){
            throw new Exception("file " + fileName + " is empty");
        }

        return JsonConverter.convertFromJson(json, HttpResult.class);
    }

    private String getFullDirectory(long crc32DomainUrl){
        return directory.
            concat("/").
            concat(String.valueOf(crc32DomainUrl)).
            concat("/")
        ;
    }

    private String getFullPathForUrl(long crc32DomainUrl, long crc32){
        return getFullDirectory(crc32DomainUrl)
            .concat("/")
            .concat(String.valueOf(crc32))
        ;
    }
}