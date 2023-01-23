package com.domainsurvey.crawler.dto;

import com.domainsurvey.crawler.service.urlProcessor.model.EdgeMetaData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkData implements Serializable {
    private static final long serialVersionUID = -4527345311914260163L;

    protected String id;
    protected String url;
    protected short depth;
    private Short statusCode;
    private boolean robotsValid;
    private EdgeMetaData metaData;
    @Builder.Default
    protected List<RedirectedLink> redirectedLinks = new ArrayList<>();
}