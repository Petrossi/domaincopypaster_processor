package com.domainsurvey.crawler.service.dao.mapper;

import com.domainsurvey.crawler.dto.PageData;
import com.domainsurvey.crawler.dto.RedirectedLink;
import com.domainsurvey.crawler.service.urlProcessor.model.SavedMetaData;
import com.domainsurvey.crawler.utils.JsonConverter;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PageDataMapper implements RowMapper<PageData> {

    @Override
    public PageData mapRow(ResultSet rs, int rowNum) throws SQLException {

        List<Integer> filters = new ArrayList<>();
        SavedMetaData savedMetaData = new SavedMetaData();

        String filtersStr = rs.getString("filters");

        if (filtersStr != null && !filtersStr.isEmpty() && !filtersStr.equals("{}")) {
            filtersStr = filtersStr.replace("}", "").replace("{", "").replaceAll(" ", "");
            filters = Arrays.stream(filtersStr.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        }
        String savedMetaDataString = rs.getString("saved_meta_data");
        if (savedMetaDataString != null && !savedMetaDataString.isEmpty()) {
            savedMetaData = JsonConverter.convertFromJson(savedMetaDataString, SavedMetaData.class);
        }

        String redirectedLinksString = rs.getString("redirected_links");

        List<RedirectedLink> redirectedLinks = Arrays.asList(JsonConverter.convertFromJson(redirectedLinksString, RedirectedLink[].class));

        return PageData
                .builder()
                .id(rs.getString("id"))
                .url(rs.getString("url"))
                .statusCode(rs.getShort("status_code"))
                .robotsValid(rs.getBoolean("robots_valid"))
                .weight(rs.getDouble("weight"))
                .incomingCountTotal(rs.getLong("incoming_count_total"))
                .filters(filters)
                .savedMetaData(savedMetaData)
                .redirectedLinks(redirectedLinks)
                .build();
    }
}