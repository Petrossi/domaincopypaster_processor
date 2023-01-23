package com.domainsurvey.crawler.service.urlProcessor.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

import org.jsoup.nodes.Element;

@Getter
@Setter
@NoArgsConstructor
public class EdgeMetaData {

    private String title;
    private String hreflang;
    private String anchor;
    private String alt;
    private boolean nofollow;
    private boolean index;
    private boolean noopener;
    private boolean noreferrer;

    public EdgeMetaData init(Element link) {
        title = link.hasAttr("title") ? link.attr("title").replace("'", "''") : "";
        alt = link.hasAttr("alt") ? link.attr("alt").replace("'", "''") : "";

        anchor = link.text();

        hreflang = link.hasAttr("hreflang") ? link.attr("hreflang") : "";

        if (link.hasAttr("rel")) {
            List<String> rel = Arrays.asList(link.attr("rel").toLowerCase().split(" "));

            nofollow = rel.contains("nofollow");
            index = rel.contains("index");
            noopener = rel.contains("noopener");
            noreferrer = rel.contains("noreferrer");
        }

        return this;
    }
}