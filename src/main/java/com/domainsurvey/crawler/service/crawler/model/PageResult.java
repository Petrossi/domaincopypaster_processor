package com.domainsurvey.crawler.service.crawler.model;

import com.domainsurvey.crawler.dto.RedirectedLink;
import com.domainsurvey.crawler.exception.MaxRedirectCountException;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.filter.FilterConfig;
import com.domainsurvey.crawler.model.link.Edge;
import com.domainsurvey.crawler.model.link.Node;
import com.domainsurvey.crawler.model.page.Page;
import com.domainsurvey.crawler.model.type.FilterImportance;
import com.domainsurvey.crawler.model.type.NodeType;
import com.domainsurvey.crawler.service.fetcher.model.HttpResult;
import com.domainsurvey.crawler.service.filter.FilterParserService;
import com.domainsurvey.crawler.service.robots.RobotsTxtParserService;
import com.domainsurvey.crawler.service.urlProcessor.model.EdgeMetaData;
import com.domainsurvey.crawler.service.urlProcessor.model.HashedMetaData;
import com.domainsurvey.crawler.service.urlProcessor.model.PageMetaData;
import com.domainsurvey.crawler.service.urlProcessor.model.SavedMetaData;
import com.domainsurvey.crawler.utils.robots.SimpleRobotRules;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.domainsurvey.crawler.service.filter.FilterParserService.ANY_ISSUE_ID;
import static com.domainsurvey.crawler.service.filter.FilterParserService.ERROR_ISSUE_FILTER_ID;
import static com.domainsurvey.crawler.service.filter.FilterParserService.NOTICE_ISSUE_FILTER_ID;
import static com.domainsurvey.crawler.service.filter.FilterParserService.NO_ISSUE_FILTER_ID;
import static com.domainsurvey.crawler.service.filter.FilterParserService.WARNING_ISSUE_FILTER_ID;
import static com.domainsurvey.crawler.utils.Constants.HTTP_EQUIV_REDIRECT;
import static com.domainsurvey.crawler.utils.UrlHelper.getValidUrl;
import static com.domainsurvey.crawler.utils.UrlHelper.isUrlValidForDomain;
import static com.domainsurvey.crawler.utils.Utils.getCRC32;
import static com.domainsurvey.crawler.utils.Utils.isApplicationLink;
import static com.domainsurvey.crawler.utils.Utils.isRedirected;
import static com.domainsurvey.crawler.utils.Utils.isSuccess;
import static com.domainsurvey.crawler.utils.Utils.isValidHttpGetContentType;

@NoArgsConstructor
@Getter
@Setter
public class PageResult {
    private PageMetaData pageMetaData;
    private Set<Node> nodes = new HashSet<>();
    private List<Edge> edges = new ArrayList<>();
    public Map<String, String> originalToValid = new HashMap<>();
    private List<Integer> filters = new ArrayList<>();
    private NodeType nodeType;
    protected long id;
    protected String url;
    protected short statusCode;
    protected boolean robotsValid;
    protected boolean cache;

    protected short depth;
    protected byte redirectCount;

    protected SavedMetaData savedMetaData;
    protected HashedMetaData hashedMetaData;

    private boolean error;
    private boolean warning;
    private boolean notice;
    protected short score;
    protected int internalCountTotal;
    protected int externalCountTotal;
    protected long loadTime;
    protected long realTime;

    public static Builder builder() {
        return new Builder();
    }

    public Page toPage() {
        return new Page(this);
    }

    public void markByPageMetaData(PageMetaData pageMetaData) {
        this.pageMetaData = pageMetaData;
        this.statusCode = pageMetaData.getStatusCode();
        this.robotsValid = pageMetaData.isRobotsValid();
        setUrl(pageMetaData.getUrl());
        markFilters();
        this.savedMetaData = pageMetaData.toSavedMetaData();
    }

    public void markFilters() {
        List<Integer> result = new ArrayList<>();
        short totalImpact = 0;

        for (FilterConfig filterConfig : FilterParserService.predicateFilterList) {
            if (filterConfig.filter.test(pageMetaData)) {
                result.add(filterConfig.id);

                if (filterConfig.importance.equals(FilterImportance.ERROR)) {
                    error = true;
                } else if (filterConfig.importance.equals(FilterImportance.WARNING)) {
                    warning = true;
                } else if (filterConfig.importance.equals(FilterImportance.NOTICE)) {
                    notice = true;
                }

                totalImpact += filterConfig.impact;
            }
        }

        if (error) {
            result.add(ERROR_ISSUE_FILTER_ID);
        }
        if (warning) {
            result.add(WARNING_ISSUE_FILTER_ID);
        }
        if (notice) {
            result.add(NOTICE_ISSUE_FILTER_ID);
        }
        if (error || warning || notice) {
            result.add(ANY_ISSUE_ID);
        }
        if (!error && !warning && !notice) {
            result.add(NO_ISSUE_FILTER_ID);
        }

        filters = result;

        score = (short) (totalImpact >= 100 ? 0 : 100 - totalImpact);
    }

    @Log4j2
    public static class Builder {
        private final PageResult pageResult = new PageResult();
        private PageMetaData pageMetaData;

        private HttpResult httpResult;
        private Node nodeToProcess;
        private Domain domain;
        private String domainUrl;
        private RobotsTxtParserService robotsTxtParserService;
        private int internalCountTotal;
        private int externalCountTotal;
        private final List<RedirectedLink> linkToProcessRedirectedLinks = new ArrayList<>();

        private Document doc;

        public Builder httpResult(HttpResult httpResult) {
            this.httpResult = httpResult;
            return this;
        }

        public Builder linkToProcess(Node nodeToProcess) {
            this.nodeToProcess = nodeToProcess;
            return this;
        }

        public Builder domain(Domain domain) {
            this.domain = domain;
            return this;
        }

        public Builder robotsTxtParserService(RobotsTxtParserService robotsTxtParserService) {
            this.robotsTxtParserService = robotsTxtParserService;
            return this;
        }

        void processLink(String linkUrl, EdgeMetaData edgeMetaData) {
            boolean internal = isUrlValidForDomain(linkUrl, domainUrl);
            boolean robotsValid = true;
            if (internal && robotsTxtParserService != null) {
                SimpleRobotRules.RobotRule rule = robotsTxtParserService.isAllowed(linkUrl);

                robotsValid = rule != null && rule._allow;
            }

            long sourceId = getCRC32(nodeToProcess.getUrl());
            long targetId = getCRC32(linkUrl);

            NodeType type;

            if (internal) {
                type = NodeType.INTERNAL;
                internalCountTotal++;
            } else {
                type = NodeType.EXTERNAL;
                externalCountTotal++;
            }

            pageResult.nodes.add(
                    Node.builder()
                            .id(targetId)
                            .url(linkUrl)
                            .type(type)
                            .depth((short) (nodeToProcess.getDepth() + 1))
                            .redirectCount((byte) linkToProcessRedirectedLinks.size())
                            .robotsValid(robotsValid)
                            .redirectedLinks(linkToProcessRedirectedLinks)
                            .build()
            );

            pageResult.edges.add(Edge.builder().targetId(targetId).sourceId(sourceId).metaData(edgeMetaData).build());
        }

        private void processLinks() {
            Elements allLinks = doc.select("a");

            var originalToValid = new HashMap<String, String>();

            for (Element element : allLinks) {
                String originUrl = element.attr("href").replace(" ", "").trim();
                boolean needsDecode = originUrl.contains("#");
                if (needsDecode) {
                    continue;
                }
                EdgeMetaData metaData = new EdgeMetaData().init(element);

                if (originUrl.equals("") || isApplicationLink(originUrl) || originUrl.contains("javascript:void")) {
                    continue;
                }
                String elementUrl = getValidUrl(originUrl, nodeToProcess.getUrl());

                try {
                    String path = new URL(elementUrl).getPath();

                    if (path.isEmpty()) {
                        elementUrl += "/";
                    }
                } catch (Exception ignored) {
                }

                originalToValid.put(originUrl, elementUrl);
                processLink(elementUrl, metaData);
            }

            pageResult.originalToValid = originalToValid;

            if (!domain.getConfig().isIgnoreRobots()) {
                pageResult.nodes = pageResult.nodes
                        .stream()
                        .filter(link -> !link.isRobotsValid())
                        .collect(Collectors.toSet())
                ;
            }
        }

        private void addResourceLink(String originUrl, NodeType nodeType, EdgeMetaData edgeMetaData) {
            String url = getValidUrl(originUrl, nodeToProcess.getUrl());
            boolean internal = isUrlValidForDomain(url, domainUrl);
            boolean robotsValid = true;

            if (internal && robotsTxtParserService != null) {
                SimpleRobotRules.RobotRule rule = robotsTxtParserService.isAllowed(url);

                robotsValid = rule != null && rule._allow;
            }

            if (url.startsWith("data:")) {
                return;
            }
            long sourceId = getCRC32(nodeToProcess.getUrl());
            long targetId = getCRC32(url);

            pageResult.nodes.add(
                    Node.builder()
                            .id(targetId)
                            .url(url)
                            .robotsValid(robotsValid)
                            .type(nodeType)
                            .depth((short) (nodeToProcess.getDepth() + 1))
                            .build()
            );

            pageResult.edges.add(Edge.builder().targetId(targetId).sourceId(sourceId).metaData(edgeMetaData).build());
        }

        private void processImages() {
            Elements img = doc.getElementsByTag("img");

            for (Element el : img) {
                String originUrl = el.attr("src");

                addResourceLink(originUrl, NodeType.IMAGE, new EdgeMetaData().init(el));
            }
        }

        private void processScripts() {
            Elements scripts = doc.select("script[src]");

            for (Element el : scripts) {

                String originUrl = el.attr("src");

                addResourceLink(originUrl, NodeType.JS, new EdgeMetaData().init(el));
            }
        }

        private void processCss() {
            Elements css = doc.select("link[rel=stylesheet]");

            for (Element el : css) {
                String originUrl = el.attr("href").split("\\?")[0];

                addResourceLink(originUrl, NodeType.CSS, new EdgeMetaData().init(el));
            }
        }

        void init() {
            domainUrl = domain.getUrl();
            pageResult.id = nodeToProcess.getId();
            pageResult.nodeType = nodeToProcess.getType();
            pageResult.statusCode = httpResult.httpStatusCode;
            pageResult.cache = httpResult.loadTime != httpResult.realTime;
            pageResult.loadTime = httpResult.loadTime;
            pageResult.realTime = httpResult.realTime;
            pageResult.redirectCount = (byte) (nodeToProcess.getRedirectCount() + 1);

            if (nodeToProcess.getRedirectedLinks() != null) {
                this.linkToProcessRedirectedLinks.addAll(nodeToProcess.getRedirectedLinks());
            }
        }

        void initPageMetaData() {
            boolean robotsValid = true;
            String robotsRule = "";

            if (robotsTxtParserService != null) {
                SimpleRobotRules.RobotRule rule = robotsTxtParserService.isAllowed(nodeToProcess.getUrl());

                if (rule != null) {
                    robotsValid = rule._allow;
                    robotsRule = rule._prefix;
                }
            }

            pageMetaData = PageMetaData.builder1()
                    .html(httpResult.html)
                    .statusCode(pageResult.statusCode)
                    .url(nodeToProcess.getUrl())
                    .charset(httpResult.charsetStr)
                    .contentType(httpResult.contentType)
                    .robotsValid(robotsValid)
                    .robotsRule(robotsRule)
                    .loadTime(httpResult.loadTime)
                    .location(httpResult.location)
                    .proxyEnabled(StringUtils.isBlank(httpResult.proxy))
                    .build();
        }

        private PageResult buildAsInternal() throws MaxRedirectCountException {
            if (isRedirected(pageResult.statusCode) || pageResult.statusCode == HTTP_EQUIV_REDIRECT) {
                if (pageResult.redirectCount >= 5) {
                    throw new MaxRedirectCountException();
                }

                httpResult.location = getValidUrl(httpResult.location, nodeToProcess.getUrl());

                linkToProcessRedirectedLinks.add(
                        RedirectedLink.builder()
                                .id(nodeToProcess.getId())
                                .url(nodeToProcess.getUrl())
                                .code(pageResult.statusCode)
                                .index((short) (linkToProcessRedirectedLinks.size() + 1))
                                .build()
                );
                processLink(httpResult.location, null);
            } else if (isSuccess(pageResult.statusCode)) {
                if (isValidHttpGetContentType(httpResult.contentType)) {
                    doc = Jsoup.parse(httpResult.html);

                    if (pageMetaData.getHttpEquivRefreshLocation() != null && !pageMetaData.getHttpEquivRefreshLocation().isEmpty()) {
                        httpResult.httpStatusCode = HTTP_EQUIV_REDIRECT;
                        httpResult.location = pageMetaData.getHttpEquivRefreshLocation();

                        return builder()
                                .httpResult(httpResult)
                                .domain(domain)
                                .linkToProcess(nodeToProcess)
                                .robotsTxtParserService(robotsTxtParserService)
                                .build();
                    }

                    processLinks();
                    processImages();
                    processScripts();
                    processCss();

                    pageMetaData.setInternalCountTotal(internalCountTotal);
                    pageMetaData.setExternalCountTotal(externalCountTotal);
                }
            }

            pageMetaData.setNodes(pageResult.nodes);

            pageResult.setPageMetaData(pageMetaData);
            pageResult.setSavedMetaData(pageMetaData.toSavedMetaData());

            pageResult.markFilters();
            pageResult.setUrl(pageMetaData.getUrl());
            return pageResult;
        }

        public PageResult build() throws MaxRedirectCountException {
            init();
            initPageMetaData();

            if (nodeToProcess.getType().equals(NodeType.INTERNAL)) {
                return buildAsInternal();
            }

            return pageResult;
        }
    }
}