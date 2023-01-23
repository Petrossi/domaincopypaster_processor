package com.domainsurvey.crawler.service.dao.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import com.domainsurvey.crawler.dto.LinkData;
import com.domainsurvey.crawler.dto.RedirectedLink;
import com.domainsurvey.crawler.service.urlProcessor.model.EdgeMetaData;
import com.domainsurvey.crawler.utils.JsonConverter;

public class LinkDataMapper implements RowMapper<LinkData> {

    @Override
    public LinkData mapRow(ResultSet rs, int rowNum) throws SQLException {
        EdgeMetaData metaData = new EdgeMetaData();
        List<RedirectedLink> redirectedLinks = new ArrayList<>();

        String metaDataString = rs.getString("meta_data");
        if (metaDataString != null && !metaDataString.isEmpty()) {
            metaData = JsonConverter.convertFromJson(metaDataString, EdgeMetaData.class);
        }

        return LinkData
                .builder()
                .id(rs.getString("id"))
                .url(rs.getString("url"))
                .depth(rs.getShort("depth"))
                .robotsValid(rs.getBoolean("robots_valid"))
                .metaData(metaData)
                .redirectedLinks(redirectedLinks)
                .statusCode(rs.getShort("status_code"))
                .build();
    }
}