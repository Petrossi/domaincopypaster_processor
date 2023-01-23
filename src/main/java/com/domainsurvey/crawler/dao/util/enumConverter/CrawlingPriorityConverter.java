package com.domainsurvey.crawler.dao.util.enumConverter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.domainsurvey.crawler.model.type.CrawlingPriority;

@Converter
public class CrawlingPriorityConverter implements AttributeConverter<CrawlingPriority, Integer> {

    @Override
    public Integer convertToDatabaseColumn(CrawlingPriority crawlingPriority) {
        return (int) crawlingPriority.getValue();
    }

    @Override
    public CrawlingPriority convertToEntityAttribute(Integer dbData) {
        return CrawlingPriority.fromValue(dbData.byteValue());
    }
}