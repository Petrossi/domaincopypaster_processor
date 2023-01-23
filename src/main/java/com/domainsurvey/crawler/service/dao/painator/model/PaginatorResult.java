package com.domainsurvey.crawler.service.dao.painator.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class PaginatorResult<T> {
    private List<T> data;
    private int total;
    private int filtered;
}