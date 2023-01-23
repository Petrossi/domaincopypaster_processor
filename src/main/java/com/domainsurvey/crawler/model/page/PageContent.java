package com.domainsurvey.crawler.model.page;

import lombok.Data;

import java.io.Serializable;

@Data
//@Entity(name = "content")
public class PageContent implements Serializable {
    private static final long serialVersionUID = -4527345351916260163L;

    public static final String TABLE_PREFIX = "content";

//    @Id
//    @Column(name = "id", nullable = false)
//    protected long id;
//
//    @Column(name = "data")
//    private Short data;
//
//    @Column(name = "hashed_data")
//    private Short data;
}