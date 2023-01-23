package com.domainsurvey.crawler.web.ws.model.request.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class PaginationRequest extends MessageRequest {
    protected String sortBy = "asc";
    protected String sortParam = "";
    protected int page = 1;
    protected int pageSize = 10;
    protected Map<String, String> additionalData = new HashMap<>();
}