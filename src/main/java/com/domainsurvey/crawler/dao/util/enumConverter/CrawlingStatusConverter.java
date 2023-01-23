package com.domainsurvey.crawler.dao.util.enumConverter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.domainsurvey.crawler.model.type.CrawlingStatus;

@Converter
public class CrawlingStatusConverter implements AttributeConverter<CrawlingStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(CrawlingStatus crawlingStatus) {
        return (int) crawlingStatus.getValue();
    }

    @Override
    public CrawlingStatus convertToEntityAttribute(Integer dbData) {
        return CrawlingStatus.fromValue(dbData.byteValue());
    }
}