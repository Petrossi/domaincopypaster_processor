package com.domainsurvey.crawler.service.dao.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import com.domainsurvey.crawler.model.page.Page;

public class PageInQueueRowMapper implements RowMapper<Page> {

    @Override
    public Page mapRow(ResultSet rs, int rowNum) throws SQLException {
        Page urlFromQueue = new Page();
        urlFromQueue.setId(rs.getLong("id"));

        return urlFromQueue;
    }
}