package com.domainsurvey.crawler.service.dao.page.impl;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.service.dao.page.FinalPageService;
import com.domainsurvey.crawler.service.table.type.SchemaType;
import org.springframework.stereotype.Service;

@Service
public class FinalPageServiceImpl extends BasePageServiceImpl implements FinalPageService {

    @Override
    public void insertFromLastTable(Domain domain) {

    }

    @Override
    protected SchemaType schemaType() {
        return SchemaType.FINAL;
    }
}