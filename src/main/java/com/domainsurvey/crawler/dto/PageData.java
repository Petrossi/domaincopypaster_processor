package com.domainsurvey.crawler.dto;

import com.domainsurvey.crawler.service.crawler.model.PageResult;
import com.domainsurvey.crawler.service.urlProcessor.model.SavedMetaData;
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
public class PageData implements Serializable {
    private static final long serialVersionUID = -4527345351914260163L;

    protected String id;
    protected String url;
    protected short depth;
    private Short statusCode;
    private List<Integer> filters;
    private boolean robotsValid;
    private boolean error;
    private boolean warning;
    private boolean notice;
    private double weight;
    private double normalizedWeight;
    protected long incomingCountTotal;
    protected short score;
    private SavedMetaData savedMetaData;
    @Builder.Default
    private List<RedirectedLink> redirectedLinks = new ArrayList<>();

    public PageData(PageResult pageResult) {
        this.id = String.valueOf(pageResult.getId());
        this.url = pageResult.getUrl();
        this.depth = pageResult.getDepth();
        this.statusCode = pageResult.getStatusCode();
        this.filters = pageResult.getFilters();
        this.robotsValid = pageResult.isRobotsValid();
        this.error = pageResult.isError();
        this.warning = pageResult.isWarning();
        this.notice = pageResult.isNotice();
        this.score = pageResult.getScore();
        this.savedMetaData = pageResult.getSavedMetaData();
    }
}