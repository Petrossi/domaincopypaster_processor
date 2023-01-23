package com.domainsurvey.crawler.service.urlProcessor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
@Builder
public class SavedMetaData {
    @Builder.Default
    protected String location = "";
    @Builder.Default
    protected long crc32Location = 0;
    @Builder.Default
    protected String title = "";
    @Builder.Default
    protected String description = "";
    @Builder.Default
    protected String h1 = "";
    @Builder.Default
    protected String canonical = "";
    @Builder.Default
    protected String contentType = "";
    @Builder.Default
    protected String robotsMetaTag = "";
    protected int externalCount;
    protected int internalCount;
    private boolean robotsValid;
    private boolean metaNoindex;
    @Builder.Default
    protected String robotsRule = "";
    @Builder.Default
    protected String h1TagsData = "[]";
    private int textContentLength;
    private int contentLength;
    private long loadTime;

    public HashedMetaData toHashedMetaData() {
        return HashedMetaData.builder().canonical(canonical).description(description).h1(h1).title(title).build();
    }
}