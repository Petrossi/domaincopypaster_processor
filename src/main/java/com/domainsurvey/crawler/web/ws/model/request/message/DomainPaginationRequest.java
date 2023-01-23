package com.domainsurvey.crawler.web.ws.model.request.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Getter
public class DomainPaginationRequest extends PaginationRequest {
    protected String id;
}