package com.domainsurvey.crawler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RedirectedLink implements Serializable {
    private static final long serialVersionUID = -4527345341914260163L;

    short index;
    String url;
    long id;
    long location;
    short code;
}