package com.domainsurvey.crawler.web.rest;

import lombok.RequiredArgsConstructor;

import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.domainsurvey.crawler.service.fetcher.FetcherProcessor;

@CrossOrigin()
@RestController
@RequestMapping(value = "/utils")
@RequiredArgsConstructor
public class DefaultController {

    private final FetcherProcessor fetcherProcessor;

    @GetMapping(value = "/getValidDomainProtocol", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public String getValidFirstDomainUrl(@RequestParam(value = "url") String url) throws Exception {
        return new JSONObject().put("url", fetcherProcessor.getValidDomainProtocol(url)).toString();
    }
}