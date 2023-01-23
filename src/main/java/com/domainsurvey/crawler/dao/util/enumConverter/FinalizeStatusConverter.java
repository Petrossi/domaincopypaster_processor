package com.domainsurvey.crawler.dao.util.enumConverter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.domainsurvey.crawler.model.type.FinalizerStatus;

@Converter
public class FinalizeStatusConverter implements AttributeConverter<FinalizerStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(FinalizerStatus status) {
        return (int) status.getValue();
    }

    @Override
    public FinalizerStatus convertToEntityAttribute(Integer dbData) {
        return FinalizerStatus.fromValue(dbData.byteValue());
    }
}