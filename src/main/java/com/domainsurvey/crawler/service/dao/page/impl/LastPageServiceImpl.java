package com.domainsurvey.crawler.service.dao.page.impl;

import com.domainsurvey.crawler.service.dao.page.LastPageService;
import com.domainsurvey.crawler.service.table.type.SchemaType;
import org.springframework.stereotype.Service;

@Service
public class LastPageServiceImpl extends BasePageServiceImpl implements LastPageService {
    @Override
    protected SchemaType schemaType() {
        return SchemaType.LAST;
    }
}