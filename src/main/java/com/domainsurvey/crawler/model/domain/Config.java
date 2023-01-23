package com.domainsurvey.crawler.model.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity(name = "config")
@Data
@NoArgsConstructor
public class Config implements Serializable {
    private static final long serialVersionUID = -452795451914260163L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(mappedBy = "config", optional = false)
    private Domain domain;

    @Column(name = "ignore_robots", nullable = false)
    private boolean ignoreRobots;

    @Column(name = "spa", nullable = false)
    private boolean spa;

    @Column(name = "pages_limit")
    private int pagesLimit;

    @Column(name = "thread_count", columnDefinition = "smallint")
    private byte threadCount;

    @Column(name = "robots_content", columnDefinition = "text")
    private String robotsContent;

    public Config(Builder builder) {
        this.ignoreRobots = builder.ignoreRobots;
        this.spa = builder.spa;
        this.pagesLimit = builder.pagesLimit;
        this.threadCount = builder.threadCount;
        this.robotsContent = builder.robotsContent;
    }

    public static Builder builder() {
        return new Builder();
    }

    @NoArgsConstructor
    public static class Builder {
        private boolean ignoreRobots;
        private boolean spa;
        private int pagesLimit;
        private byte threadCount;
        private String robotsContent;

        public Builder ignoreRobots(boolean ignoreRobots) {
            this.ignoreRobots = ignoreRobots;
            return this;
        }

        public Builder spa(boolean spa) {
            this.spa = spa;
            return this;
        }

        public Builder pagesLimit(int pagesLimit) {
            this.pagesLimit = pagesLimit;
            return this;
        }

        public Builder threadCount(int threadCount) {
            return threadCount((byte) threadCount);
        }

        public Builder threadCount(byte threadCount) {
            this.threadCount = threadCount;
            return this;
        }

        public Builder robotsContent(String robotsContent) {
            this.robotsContent = robotsContent;
            return this;
        }

        public Config build() {
            return new Config(this);
        }
    }
}