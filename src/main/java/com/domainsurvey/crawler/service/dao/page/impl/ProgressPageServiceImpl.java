package com.domainsurvey.crawler.service.dao.page.impl;

import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.page.Page;
import com.domainsurvey.crawler.service.dao.page.ProgressPageService;
import com.domainsurvey.crawler.service.table.type.SchemaType;

@Service
public class ProgressPageServiceImpl extends BasePageServiceImpl implements ProgressPageService {

    @Override
    public void create(Page page, Domain domain) {
        String query = String.format(
                "insert into %s (%s) values (%s)",
                tableName(domain.getId()),
                "id, url, depth, redirect_count, robots_valid",
                "?, ?, ?, ?, ?, ?"
        );
    }

    @Override
    protected SchemaType schemaType() {
        return SchemaType.PROCESS;
    }
}