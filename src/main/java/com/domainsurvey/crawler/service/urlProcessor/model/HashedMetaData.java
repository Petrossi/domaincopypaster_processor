package com.domainsurvey.crawler.service.urlProcessor.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.domainsurvey.crawler.utils.Utils;

@Data
@NoArgsConstructor
public class HashedMetaData {
    protected long title;
    protected long description;
    protected long h1;
    protected long canonical;

    public HashedMetaData(Builder builder) {
        this.title = builder.title;
        this.description = builder.description;
        this.h1 = builder.h1;
        this.canonical = builder.canonical;
    }

    public static Builder builder() {
        return new Builder();
    }

    @NoArgsConstructor
    public static class Builder {
        protected long title;
        protected long description;
        protected long h1;
        protected long canonical;

        public Builder title(String title) {
            this.title = Utils.getCRC32(title);
            return this;
        }

        public Builder description(String description) {
            this.description = Utils.getCRC32(description);
            return this;
        }

        public Builder h1(String h1) {
            this.h1 = Utils.getCRC32(h1);
            return this;
        }

        public Builder canonical(String canonical) {
            this.canonical = Utils.getCRC32(canonical);
            return this;
        }

        public HashedMetaData build() {
            return new HashedMetaData(this);
        }
    }
}