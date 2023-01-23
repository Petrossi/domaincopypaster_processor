package com.domainsurvey.crawler.model.filter;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.domainsurvey.crawler.dao.util.enumConverter.CommonVersionConverter;
import com.domainsurvey.crawler.model.type.CommonVersion;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity(name = "domain_filter")
@NoArgsConstructor
public class DomainFilter implements Serializable {

    private static final long serialVersionUID = -452123251914260163L;

    public static int FINAL_PAGE_VERSION_NEW = 1;
    public static int FINAL_PAGE_VERSION_OLD = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @JsonIgnore
    @Column(name = "domain_crawling_info_id", nullable = false)
    private long domainCrawlingInfoId;

    @Column(name = "filter_id", nullable = false)
    private long filterId;

    @Column(name = "count", nullable = false)
    private int count;

    @Convert(converter = CommonVersionConverter.class)
    @Column(name = "version", nullable = false, columnDefinition = "smallint")
    private CommonVersion version;

    public DomainFilter(Builder builder) {
        this.domainCrawlingInfoId = builder.domainCrawlingInfoId;
        this.filterId = builder.filterId;
        this.count = builder.count;
        this.version = builder.version;
    }

    public static Builder builder() {
        return new Builder();
    }

    @NoArgsConstructor
    public static class Builder {
        private long domainCrawlingInfoId;
        private long filterId;
        private int count;
        private CommonVersion version;

        public Builder domainCrawlingInfoId(long domainCrawlingInfoId) {
            this.domainCrawlingInfoId = domainCrawlingInfoId;
            return this;
        }

        public Builder filterId(long filterId) {
            this.filterId = filterId;
            return this;
        }

        public Builder count(int count) {
            this.count = count;
            return this;
        }

        public Builder version(CommonVersion version) {
            this.version = version;
            return this;
        }

        public DomainFilter build() {
            return new DomainFilter(this);
        }
    }
}