package com.domainsurvey.crawler.model.filter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.domainsurvey.crawler.model.type.FilterImportance;
import com.domainsurvey.crawler.service.urlProcessor.model.PageMetaData;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterConfig {

    public Predicate<PageMetaData> filter;
    public List<String> where;
    public int id;
    public FilterImportance importance;
    public int impact;
    public String title;
    public Function<String, String> bodyQuery;
}