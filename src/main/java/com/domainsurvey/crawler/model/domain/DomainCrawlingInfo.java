package com.domainsurvey.crawler.model.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import static com.domainsurvey.crawler.utils.Utils.getCurrentTimestamp;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;

import com.domainsurvey.crawler.dao.util.enumConverter.FinalizeStatusConverter;
import com.domainsurvey.crawler.model.type.FinalizerStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;

@EqualsAndHashCode()
@Data
@NoArgsConstructor
@Entity(name = "domain_crawling_info")
public class DomainCrawlingInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(optional = false, fetch = FetchType.EAGER)
    private Domain domain;

    @Convert(converter = FinalizeStatusConverter.class)
    @Column(name = "finalize_status", nullable = false, columnDefinition = "smallint")
    private FinalizerStatus finalizerStatus;

    @Column(name = "hard_retry_count", nullable = false, columnDefinition = "smallint")
    private byte hardRetryCount;

    @Column(name = "queue_added_timestamp")
    private Timestamp queueAddedTimestamp;

    @Column(name = "crawling_started_timestamp")
    private Timestamp crawlingStartedTimestamp;

    @Column(name = "crawling_finished_timestamp")
    private Timestamp crawlingFinishedTimestamp;

    @Column(name = "finalizer_started_timestamp")
    private Timestamp finalizerStartedTimestamp;

    @Column(name = "finalizer_finished_timestamp")
    private Timestamp finalizerFinishedTimestamp;

    @Column(name = "total")
    protected long total;

    @Column(name = "blocked")
    protected long blocked;

    @Column(name = "in_queue")
    protected long inQueue;

    @Column(name = "error")
    protected long error;

    @Column(name = "warning")
    protected long warning;

    @Column(name = "notice")
    protected long notice;

    @Column(name = "no_issue")
    protected long noIssue;

    @Column(name = "score", columnDefinition = "smallint")
    protected byte score;

    @PrePersist
    protected void onCreate() {
        queueAddedTimestamp = getCurrentTimestamp();
    }

    public double minutesFromStart() {
        long timeDiff = (getCurrentTimestamp().getTime() - crawlingStartedTimestamp.getTime()) / 1000;

        return (double) timeDiff / 60;
    }

    public DomainCrawlingInfo(Builder builder) {
        this.domain = builder.domain;
        this.finalizerStatus = FinalizerStatus.NOT_COUNTING;
    }

    public static Builder builder() {
        return new Builder();
    }

    @NoArgsConstructor
    public static class Builder {
        private Domain domain;

        public Builder domain(Domain domain) {
            this.domain = domain;
            return this;
        }

        public DomainCrawlingInfo build() {
            return new DomainCrawlingInfo(this);
        }
    }
}