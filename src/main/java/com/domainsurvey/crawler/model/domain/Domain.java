package com.domainsurvey.crawler.model.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import static com.domainsurvey.crawler.utils.Utils.getCurrentTimestamp;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;

import com.domainsurvey.crawler.dao.util.DomainIdGenerator;
import com.domainsurvey.crawler.dao.util.enumConverter.CrawlingPriorityConverter;
import com.domainsurvey.crawler.dao.util.enumConverter.CrawlingStatusConverter;
import com.domainsurvey.crawler.model.type.CrawlingPriority;
import com.domainsurvey.crawler.model.type.CrawlingStatus;
import com.domainsurvey.crawler.service.backend.model.BackendDomain;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity(name = Domain.TABLE_PREFIX)
@Data
public class Domain implements Serializable {

    public static final String TABLE_PREFIX = "domains";

    private static final long serialVersionUID = -452795351914260163L;

    @Id
    @Column(name = "id", nullable = false, length = DomainIdGenerator.ID_LENGTH)
    private String id;

    @Column(name = "host", nullable = false, length = 253)
    private String host;

    @Column(name = "protocol", nullable = false, length = 5)
    private String protocol;

    @Column(name = "created_timestamp", nullable = false, updatable = false)
    private Timestamp createdTimestamp;

    @Convert(converter = CrawlingStatusConverter.class)
    @Column(name = "status", nullable = false, columnDefinition = "smallint")
    private CrawlingStatus status;

    @Convert(converter = CrawlingPriorityConverter.class)
    @Column(name = "priority", nullable = false, columnDefinition = "smallint")
    private CrawlingPriority priority;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "config_id", referencedColumnName = "id")
    private Config config;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "process_crawling_info_id", referencedColumnName = "id")
    private DomainCrawlingInfo processDomainCrawlingInfo;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "final_crawling_info_id", referencedColumnName = "id")
    private DomainCrawlingInfo finalDomainCrawlingInfo;

    @Column(name = "score", nullable = false, columnDefinition = "smallint")
    private byte score;

    @Column(name = "crawl_count", nullable = false)
    private int crawlCount;

    public Domain(Builder builder) {
        this();
        this.host = builder.host;
        this.protocol = builder.protocol;
        this.config = builder.config;
        this.id = builder.id;
    }

    public Domain() {
        this.status = CrawlingStatus.CREATED;
        this.priority = CrawlingPriority.NEW;
    }

    @PrePersist
    protected void onCreate() {
        createdTimestamp = getCurrentTimestamp();
    }

    public void setConfig(Config config) {
        config.setDomain(this);
        this.config = config;
    }

    public void setProcessDomainCrawlingInfo(DomainCrawlingInfo processDomainCrawlingInfo) {
        if (processDomainCrawlingInfo != null) {
            processDomainCrawlingInfo.setDomain(this);
        }
        this.processDomainCrawlingInfo = processDomainCrawlingInfo;
    }

    public void setFinalDomainCrawlingInfo(DomainCrawlingInfo finalDomainCrawlingInfo) {
        finalDomainCrawlingInfo.setDomain(this);
        this.finalDomainCrawlingInfo = finalDomainCrawlingInfo;
    }

    public String getUrl() {
        return protocol + "://" + host;
    }

    public static Builder builder() {
        return new Builder();
    }

    @NoArgsConstructor
    public static class Builder {
        private String id = DomainIdGenerator.generateNewId();
        private String host;
        private String protocol;
        private Config config;

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder config(Config config) {
            this.config = config;
            return this;
        }

        public Builder backendDomain(BackendDomain backendDomain) {
            this.id = backendDomain.getId();
            this.host = backendDomain.getHost();
            this.protocol = backendDomain.getProtocol();
            this.config = Config.builder()
                    .threadCount(backendDomain.getThreadCount())
                    .pagesLimit(backendDomain.getPagesLimit())
                    .ignoreRobots(backendDomain.isIgnoreRobots())
                    .build();

            return this;
        }

        public Domain build() {
            return new Domain(this);
        }
    }
}