package com.domainsurvey.crawler.dao.util.enumConverter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.domainsurvey.crawler.model.type.CommonVersion;

@Converter
public class CommonVersionConverter implements AttributeConverter<CommonVersion, Integer> {

    @Override
    public Integer convertToDatabaseColumn(CommonVersion status) {
        return (int) status.getValue();
    }

    @Override
    public CommonVersion convertToEntityAttribute(Integer dbData) {
        return CommonVersion.fromValue(dbData.byteValue());
    }
}