package com.domainsurvey.crawler.service.urlProcessor.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import static com.domainsurvey.crawler.utils.UrlHelper.getValidUrl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.domainsurvey.crawler.model.link.Node;
import com.domainsurvey.crawler.utils.Utils;

@EqualsAndHashCode(callSuper = true)
@Data
public class PageMetaData extends SavedMetaData {

    private short statusCode;
    private String url;
    private boolean robotsValid;

    private String robotsMetaTag = "";
    private String contentType = "";
    private String robotsRule = "";
    private String charset = "";
    private String favicon = "";
    private boolean index;
    private boolean follow;
    private boolean noindex;
    private boolean nofollow;
    private boolean noarchive;
    private boolean nosnippet;
    private boolean noodp;
    private boolean noydir;
    private boolean noyaca;
    private int internalCountTotal;
    private int externalCountTotal;

    private int textContentLength;
    private int contentLength;
    private int h1Count;
    private int canonicalCount;
    private int titleCount;
    private int descriptionCount;
    private int bodyCount;
    private long loadTime;
    private boolean proxyEnabled;

    private String h1TagsData = "[]";
    private String hTagsData = "[]";
    private String textContent = "";
    private String textContentMD5 = "";
    private String httpEquivRefreshLocation = "";
    private String location = "";

    private Set<Node> nodes = new HashSet<>();
    private byte redirectCount;

    public PageMetaData(Builder builder) {
        this.statusCode = builder.statusCode;
        this.url = builder.url;
        this.location = builder.location;
        this.robotsValid = builder.robotsValid;
        this.charset = builder.charset;
        this.contentType = builder.contentType;
        this.loadTime = builder.loadTime;
        this.proxyEnabled = builder.proxyEnabled;
        this.robotsRule = builder.robotsRule;

        if (builder.html != null && !builder.html.isEmpty()) {
            Document document = Jsoup.parse(builder.html);

            this.titleCount = document.select("title").size();
            if (titleCount > 0) {
                this.title = document.select("title").first().text().replace("'", "''").replaceAll("\\s+", " ");
            }
            this.contentLength = builder.html.length();
            this.textContentLength = document.text().length();

            this.h1Count = document.select("h1").size();

            if (h1Count > 0) {
                this.h1 = document.select("h1").first().text().replace("'", "''");
            }
            this.bodyCount = StringUtils.countMatches(builder.html, "<body");

            parseLinkTags(document);
            parseMetaTags(document);
            parseHTagsData(document);
        }
    }

    public SavedMetaData toSavedMetaData() {
        SavedMetaDataBuilder builder = SavedMetaData.builder().
                title(title).
                description(description).
                h1(h1).
                canonical(canonical).
                contentType(contentType).
                metaNoindex(noindex).
                externalCount(externalCountTotal).
                internalCount(internalCountTotal).
                robotsValid(robotsValid).
                robotsMetaTag(robotsMetaTag).
                robotsRule(robotsRule).
                h1TagsData(h1TagsData).
                textContentLength(textContentLength).
                contentLength(contentLength).
                loadTime(loadTime);

        if (location != null && !location.isEmpty()) {
            String location = getValidUrl(this.location, this.getUrl());
            long crc32Location = Utils.getCRC32(location);

            builder = builder.location(location).crc32Location(crc32Location);
        }

        return builder.build();
    }

    private void parseLinkTags(Document document) {
        int currentCanonicalCount = 0;
        Elements imports = document.select("link");
        for (Element link : imports) {
            if (link.hasAttr("rel") && link.hasAttr("href")) {
                String rel = link.attr("rel").toLowerCase();
                String href = link.attr("href");

                if (rel.contains("icon")) {
                    this.favicon = href;
                } else if (rel.equals("canonical")) {
                    if (currentCanonicalCount == 0) {
                        this.canonical = link.attr("href");
                    }
                    currentCanonicalCount++;
                }
            }
        }

        this.canonicalCount = currentCanonicalCount;
    }

    private void parseMetaTags(Document document) {
        Elements metaTags = document.getElementsByTag("meta");
        int currentDescriptionCount = 0;
        for (Element metaTag : metaTags) {
            if (metaTag.hasAttr("http-equiv") && metaTag.attr("http-equiv").trim().toLowerCase().equals("refresh")) {
                String locationContent = metaTag.attr("content").toLowerCase();
                if (locationContent.contains("url=")) {
                    String[] parts = locationContent.split("url=");
                    if (parts.length > 1) {
                        this.httpEquivRefreshLocation = parts[1];
                    }
                }
            }
            if (metaTag.hasAttr("property") && metaTag.attr("property").trim().equalsIgnoreCase("description")) {
                this.description = metaTag.attr("content");
                currentDescriptionCount++;
            }
            if (metaTag.hasAttr("name")) {
                String name = metaTag.attr("name").trim().toLowerCase();
                if (name.equals("description")) {
                    this.description = metaTag.attr("content");
                    currentDescriptionCount++;
                }

                try {
                    if (name.equals("robots")) {
                        String content = metaTag.attr("content");
                        this.robotsMetaTag = content;
                        parseRobotsContent(content);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        this.descriptionCount = currentDescriptionCount;
    }

    private void parseHTagsData(Document document) {
        JSONArray currentHTagsData = new JSONArray();

        for (int n = 1; n < 6; n++) {
            String tagName = "h" + n;
            Elements tags = document.select(tagName);
            JSONArray content = new JSONArray();
            tags.forEach(tag -> content.put(tag.text()));

            JSONObject tagData = new JSONObject().put("name", tagName).put("count", tags.size()).put("content", content);

            currentHTagsData.put(tagData);
            if (n == 1) {
                this.h1TagsData = content.toString();
            }
        }

        this.hTagsData = currentHTagsData.toString();
    }

    private void parseRobotsContent(String robotsContent) {
        List<String> values = Arrays.asList(robotsContent.replace(" ", "").trim().toLowerCase().split(","));

        this.noindex = values.contains("noindex");
        this.nofollow = values.contains("nofollow");
        this.index = true;
        this.follow = values.contains("follow") || values.contains("dofollow");
        this.noarchive = values.contains("noarchive");
        this.nosnippet = values.contains("nosnippet");
        this.noodp = values.contains("noodp");
        this.noydir = values.contains("noydir");
        this.noyaca = values.contains("noyaca");
    }

    public static Builder builder1() {
        return new Builder();
    }

    @NoArgsConstructor
    public static class Builder {
        private short statusCode;
        private String url = "";
        private String location = "";
        private String html = "";
        private String charset = "";
        private String contentType = "";
        private String robotsRule = "";
        private boolean robotsValid;
        private boolean proxyEnabled;
        private long loadTime;

        public Builder statusCode(short statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder html(String html) {
            this.html = html;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder robotsValid(boolean robotsValid) {
            this.robotsValid = robotsValid;
            return this;
        }

        public Builder robotsRule(String robotsRule) {
            this.robotsRule = robotsRule;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder charset(String charset) {
            this.charset = charset;
            return this;
        }

        public Builder loadTime(long loadTime) {
            this.loadTime = loadTime;
            return this;
        }

        public Builder proxyEnabled(boolean proxyEnabled) {
            this.proxyEnabled = proxyEnabled;
            return this;
        }

        public PageMetaData build() {
            return new PageMetaData(this);
        }
    }
}