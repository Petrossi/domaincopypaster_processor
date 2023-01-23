package com.domainsurvey.crawler.service.table.type;

import lombok.Getter;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.link.Edge;
import com.domainsurvey.crawler.model.link.Node;
import com.domainsurvey.crawler.model.page.Page;

@Getter
public enum TableType {

    PAGE(Page.TABLE_PREFIX),
    NODE(Node.TABLE_PREFIX),
    EDGE(Edge.TABLE_PREFIX),
    DOMAIN(Domain.TABLE_PREFIX);

    private final String value;

    TableType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}