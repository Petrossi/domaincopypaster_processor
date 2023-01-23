package com.domainsurvey.crawler.service.filter;

import com.domainsurvey.crawler.model.filter.FilterConfig;
import com.domainsurvey.crawler.model.filter.LinkFilterConfig;
import com.domainsurvey.crawler.model.link.Node;
import com.domainsurvey.crawler.model.type.FilterImportance;
import com.domainsurvey.crawler.model.type.NodeType;
import com.domainsurvey.crawler.service.table.TableService;
import com.domainsurvey.crawler.service.table.type.SchemaType;
import com.domainsurvey.crawler.service.table.type.TableType;
import com.domainsurvey.crawler.service.urlProcessor.model.PageMetaData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.domainsurvey.crawler.model.type.FilterImportance.ERROR;
import static com.domainsurvey.crawler.model.type.FilterImportance.INFO;
import static com.domainsurvey.crawler.model.type.FilterImportance.ISSUE;
import static com.domainsurvey.crawler.model.type.FilterImportance.NOTICE;
import static com.domainsurvey.crawler.model.type.FilterImportance.WARNING;

public class FilterParserService {

    public static final int ALL_PAGES_FILTER_ID = 1;
    public static final int ANY_ISSUE_ID = 2;
    public static final int NO_ISSUE_FILTER_ID = 3;
    public static final int ERROR_ISSUE_FILTER_ID = 4;
    public static final int WARNING_ISSUE_FILTER_ID = 5;
    public static final int NOTICE_ISSUE_FILTER_ID = 6;

    public static final List<FilterConfig> allFiltersList;
    public static final List<LinkFilterConfig> allLinkFiltersList;

    public static final List<FilterConfig> issuesFilterList;
    public static final List<FilterConfig> predicateFilterList;
    public static final List<FilterConfig> dbFilterList;
    public static final List<FilterConfig> impactFilterList;

    public static final Map<FilterImportance, List<Integer>> issueIdToFilterIds;

    public static final Map<Integer, FilterConfig> filters;
    public static final Map<Integer, LinkFilterConfig> linkFilters;

    public static String getWhere(Collection<FilterConfig> filterConfigs) {
        String separator = " OR ";
        List<List<String>> allFiltersPredicate = filterConfigs
                .stream()
                .map(filterConfig -> filterConfig.where)
                .collect(Collectors.toList());

        List<String> commonPredicates = new ArrayList<>();
        if (filterConfigs.size() > 1) {
            for (String firstFilterPredicate : allFiltersPredicate.get(0)) {
                boolean isCommon = true;
                for (List<String> allFilterPredicate : allFiltersPredicate) {
                    if (!allFilterPredicate.contains(firstFilterPredicate)) {
                        isCommon = false;
                        break;
                    }
                }
                if (isCommon) {
                    commonPredicates.add(firstFilterPredicate);
                }
            }
        }

        String commonWhere = commonPredicates.stream().distinct().collect(Collectors.joining(" AND "));

        String allWhereWithoutCommon = filterConfigs
                .stream()
                .map(filterConfig -> filterConfig.where.
                        stream().
                        filter(predicate -> !commonWhere.contains(predicate)).
                        distinct().
                        collect(Collectors.joining(" AND "))
                )
                .filter(currentWhere -> !currentWhere.equals(""))
                .map(currentWhere -> "(" + currentWhere + ")")
                .collect(Collectors.joining(separator));

        return !commonWhere.equals("") ? commonWhere + " AND (" + allWhereWithoutCommon + ")" : allWhereWithoutCommon;
    }

    public static String getWhere(FilterConfig filterConfig) {
        try {
            return getWhere(Collections.singletonList(filterConfig));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static Predicate<PageMetaData> isValidStatusCode() {
        return fp -> fp.getStatusCode() == 200;
    }

    public static Predicate<PageMetaData> isValidHtml() {
        return fp -> fp.getContentType().equals("text/html");
    }

    public static Predicate<PageMetaData> isValidHtmlAndStatusCode() {
        return isValidStatusCode().and(isValidHtml());
    }

    private static String getMetadataParam(String param) {
        return String.format("hashed_meta_data->>'%s'", param);
    }

    private static String getExists(String param) {
        String paramField = getMetadataParam(param);
        return String.format(
                "exists(select fpd.id from TABLE_NAME fpd where p_update.%s = fpd.%s and fpd.id != p_update.id)",
                paramField, paramField
        );
    }

    static {
        issuesFilterList = Arrays.asList(
                FilterConfig.builder().id(ALL_PAGES_FILTER_ID).title("Crawled URLs").importance(ISSUE).build(),
                FilterConfig.builder().id(ANY_ISSUE_ID).title("Issues").importance(ISSUE).build(),
                FilterConfig.builder().id(NO_ISSUE_FILTER_ID).title("No Issues").importance(ISSUE).build(),
                FilterConfig.builder().id(ERROR_ISSUE_FILTER_ID).title("Error Issues").importance(ISSUE).build(),
                FilterConfig.builder().id(WARNING_ISSUE_FILTER_ID).title("Warning Issues").importance(ISSUE).build(),
                FilterConfig.builder().id(NOTICE_ISSUE_FILTER_ID).title("Notice Issues").importance(ISSUE).build()
        );
    }

    static {
        dbFilterList = Arrays.asList(
                FilterConfig.builder().id(7).title("Duplicate Title").importance(WARNING).where(Arrays.asList(getMetadataParamNotEmpty("title"), getExists("title"))).build(),
                FilterConfig.builder().id(8).title("Duplicate H1").importance(WARNING).where(Arrays.asList(getMetadataParamNotEmpty("h1"), getExists("h1"))).build(),
                FilterConfig.builder().id(9).title("Duplicate Description").importance(WARNING).where(Arrays.asList(getMetadataParamNotEmpty("description"), getExists("description"))).build(),
                FilterConfig.builder().id(60).title("Page has links to broken page").importance(WARNING).bodyQuery((id) -> {
                    String pageTableName = TableService.getFullTableName(id, SchemaType.FINAL, TableType.PAGE);
                    String nodeTableName = TableService.getFullTableName(id, SchemaType.FINAL, TableType.NODE);
                    String edgeTableName = TableService.getFullTableName(id, SchemaType.FINAL, TableType.EDGE);

                    String template =
                            "from (" +
                                    " select " +
                                    "   distinct on (source_page.id) source_page.id as id " +
                                    " from %s source_page " +
                                    " inner join %s e on source_page.id = e.source_id " +
                                    " inner join %s target_page on target_page.id = e.target_id " +
                                    " inner join %s target_node on target_page.id = target_node.id " +
                                    " where target_node.type < %s and target_page.status_code >= 400 " +
                                    ")p " +
                                    "where p_update.id = p.id;";

                    return String.format(
                            template,
                            pageTableName, edgeTableName, pageTableName, nodeTableName, NodeType.CSS.getValue()
                    );
                }).impact(5).build(),
                FilterConfig.builder().id(76).title("Internal links: Link on page with 3xx status code").importance(WARNING).bodyQuery((id) -> {
                    String pageTableName = TableService.getFullTableName(id, SchemaType.FINAL, TableType.PAGE);
                    String nodeTableName = TableService.getFullTableName(id, SchemaType.FINAL, TableType.NODE);
                    String edgeTableName = TableService.getFullTableName(id, SchemaType.FINAL, TableType.EDGE);

                    String template =
                            "from (" +
                                    " select " +
                                    "   distinct on (source_page.id) source_page.id as id " +
                                    " from %s source_page " +
                                    " inner join %s e on source_page.id = e.source_id " +
                                    " inner join %s target_page on target_page.id = e.target_id " +
                                    " inner join %s target_node on target_page.id = target_node.id " +
                                    " where target_node.type < %s and target_page.status_code >= 300 and target_page.status_code < 400 " +
                                    ")p " +
                                    "where p_update.id = p.id;";

                    return String.format(
                            template,
                            pageTableName, edgeTableName, pageTableName, nodeTableName, NodeType.CSS.getValue()
                    );
                }).impact(5).build(),
                FilterConfig.builder().id(61).title("Redirect chain").importance(WARNING).bodyQuery((id) -> {
                    String pageTableName = TableService.getFullTableName(id, SchemaType.FINAL, TableType.PAGE);
                    String nodeTableName = TableService.getFullTableName(id, SchemaType.FINAL, TableType.NODE);

                    String template =
                            "from %s p " +
                                    "inner join %s n_update on n_update.id = p.id " +
                                    "where p.id = p_update.id " +
                                    "and jsonb_array_length(n_update.redirected_links) > 2;";

                    return String.format(
                            template,
                            pageTableName, nodeTableName
                    );
                }).impact(5).build()
        );
    }

    static {
        predicateFilterList = Arrays.asList(
                FilterConfig.builder().id(10).title("301 Redirects").importance(NOTICE).filter(fp -> fp.getStatusCode() == 301).impact(100).build(),
                FilterConfig.builder().id(11).title("Non-200 URLs").importance(INFO).filter(fp -> fp.getStatusCode() != 200).build(),
                FilterConfig.builder().id(12).title("Canonical ≠ URL").importance(WARNING).filter(isValidHtmlAndStatusCode().and(fp -> fp.getCanonical() != null && !fp.getCanonical().isEmpty() && !fp.getCanonical().equals(fp.getUrl()))).impact(20).build(),
                FilterConfig.builder().id(13).title("Canonical to non-200").importance(WARNING).filter(isValidStatusCode().negate().and(fp -> !fp.getCanonical().isEmpty())).impact(45).build(),
                FilterConfig.builder().id(14).title("Indexable Pages").importance(INFO).filter(fp -> !fp.isNoindex() && fp.isRobotsValid()).build(),
                FilterConfig.builder().id(15).title("URLs with Follow attribute").importance(INFO).filter(PageMetaData::isFollow).build(),
                FilterConfig.builder().id(16).title("Meta Nofollow Pages").importance(NOTICE).filter(PageMetaData::isNofollow).build(),
                FilterConfig.builder().id(17).title("Non-indexable Pages").importance(INFO).filter(isValidStatusCode().and(fp -> fp.isNoindex() || !fp.isRobotsValid())).build(),
                FilterConfig.builder().id(18).title("URLs with Nosnippet attribute").importance(INFO).filter(PageMetaData::isNosnippet).build(),
                FilterConfig.builder().id(19).title("URLs with Noarchive attribute").importance(INFO).filter(PageMetaData::isNoarchive).build(),
                FilterConfig.builder().id(20).title("Images").importance(INFO).filter(fp -> fp.getContentType().contains("image")).build(),
                FilterConfig.builder().id(21).title("200 URLs").importance(INFO).filter(isValidStatusCode()).build(),
                FilterConfig.builder().id(22).title("Disallowed by robots.txt").importance(NOTICE).filter(fp -> !fp.isRobotsValid() && fp.getRobotsRule() != null && !fp.getRobotsRule().isEmpty()).build(),
                FilterConfig.builder().id(23).title("Non-301 Redirects").importance(NOTICE).filter(fp -> fp.getStatusCode() >= 300 && fp.getStatusCode() < 400 && fp.getStatusCode() != 301).impact(100).build(),
                FilterConfig.builder().id(24).title("4xx Client errors").importance(WARNING).filter(fp -> fp.getStatusCode() >= 400 && fp.getStatusCode() < 500).build(),
                FilterConfig.builder().id(25).title("5xx Server errors").importance(ERROR).filter(fp -> fp.getStatusCode() >= 500 && fp.getStatusCode() < 600).build(),
                FilterConfig.builder().id(26).title("Meta Noindex Pages").importance(NOTICE).filter(PageMetaData::isNoindex).build(),
                FilterConfig.builder().id(27).title("Code Ratio < 10%").importance(NOTICE).filter(isValidHtmlAndStatusCode().and(fp -> fp.getContentLength() > 0 && (fp.getContentLength() / 100 == 0) && fp.getTextContentLength() > 0 && ((fp.getTextContentLength() / (fp.getContentLength() / 100)) < 10))).build(),
                FilterConfig.builder().id(28).title("More than one H1 on page").importance(ERROR).filter(fp -> fp.getH1Count() > 1).impact(15).build(),
                FilterConfig.builder().id(29).title("Thin pages").importance(NOTICE).filter(isValidHtmlAndStatusCode().and(fp -> fp.getContentLength() < 500)).impact(5).build(),
                FilterConfig.builder().id(30).title("Non-HTML URLs").importance(INFO).filter(isValidStatusCode().and(isValidHtml().negate())).build(),
                FilterConfig.builder().id(31).title("Long URLs").importance(NOTICE).filter(fp -> fp.getUrl().length() > 115).impact(2).build(),
                FilterConfig.builder().id(32).title("Duplicate Body").importance(ERROR).filter(fp -> fp.getBodyCount() > 1).impact(5).build(),
                FilterConfig.builder().id(33).title("Sitemap.xml").importance(INFO).filter(isValidStatusCode().and(fp -> fp.isRobotsValid() && !fp.isNoindex())).build(),
                FilterConfig.builder().id(34).title("Title is empty").importance(ERROR).filter(isValidHtmlAndStatusCode().and(fp -> fp.getTitleCount() > 0 && fp.getTitle().isEmpty())).impact(5).build(),
                FilterConfig.builder().id(35).title("Title too long").importance(NOTICE).filter(isValidHtmlAndStatusCode().and(fp -> fp.getTitle().length() > 70)).impact(5).build(),
                FilterConfig.builder().id(36).title("Title too short").importance(NOTICE).filter(isValidHtmlAndStatusCode().and(fp -> fp.getTitleCount() > 0 && fp.getTitle().length() < 30)).impact(5).build(),
                FilterConfig.builder().id(37).title("H1 is empty").importance(ERROR).filter(isValidHtmlAndStatusCode().and(fp -> fp.getH1Count() > 0 && fp.getH1().isEmpty())).impact(7).build(),
                FilterConfig.builder().id(38).title("H1 too long").importance(NOTICE).filter(isValidHtmlAndStatusCode().and(fp -> fp.getH1().length() > 70)).impact(3).build(),
                FilterConfig.builder().id(39).title("H1 too short").importance(NOTICE).filter(isValidHtmlAndStatusCode().and(fp -> fp.getH1Count() > 0 && fp.getH1().length() < 5)).impact(2).build(),
                FilterConfig.builder().id(40).title("Description is empty").importance(ERROR).filter(isValidHtmlAndStatusCode().and(fp -> fp.getDescriptionCount() > 0 && fp.getDescription().isEmpty())).impact(5).build(),
                FilterConfig.builder().id(41).title("Description too long").importance(NOTICE).filter(isValidStatusCode().and(fp -> fp.getDescription().length() > 320)).build(),
                FilterConfig.builder().id(42).title("Description too short").importance(NOTICE).filter(isValidStatusCode().and(fp -> fp.getDescription().length() > 0 && fp.getDescription().length() < 70)).impact(5).build(),
                FilterConfig.builder().id(43).title("High External Linking").importance(INFO).filter(isValidStatusCode().and(fp -> fp.getExternalCountTotal() > 10)).build(),
                FilterConfig.builder().id(44).title("PDF Files").importance(INFO).filter(fp -> fp.getContentType().equals("application/pdf")).build(),
                FilterConfig.builder().id(45).title("H1 is missing").importance(ERROR).filter(isValidHtmlAndStatusCode().and(fp -> fp.getH1Count() == 0)).impact(5).build(),
                FilterConfig.builder().id(46).title("Canonical is missing").importance(NOTICE).filter(isValidHtmlAndStatusCode().and(fp -> fp.getCanonicalCount() == 0)).impact(5).build(),
                FilterConfig.builder().id(47).title("Title is missing").importance(ERROR).filter(isValidHtmlAndStatusCode().and(fp -> fp.getTitleCount() == 0)).impact(7).build(),
                FilterConfig.builder().id(48).title("More than one Title tag on page").importance(ERROR).filter(isValidHtmlAndStatusCode().and(fp -> fp.getTitleCount() > 1)).impact(15).build(),
                FilterConfig.builder().id(49).title("Description is missing").importance(ERROR).filter(isValidHtmlAndStatusCode().and(fp -> fp.getDescriptionCount() == 0)).impact(7).build(),
                FilterConfig.builder().id(50).title("More than one Description tag on page").importance(ERROR).filter(isValidHtmlAndStatusCode().and(fp -> fp.getDescriptionCount() > 1)).build(),
                FilterConfig.builder().id(51).title("302 Redirects").importance(NOTICE).filter(fp -> fp.getStatusCode() == 302).impact(25).build(),
                FilterConfig.builder().id(52).title("Canonical is empty").importance(NOTICE).filter(isValidHtmlAndStatusCode().and(fp -> fp.getCanonicalCount() > 0 && fp.getCanonical().isEmpty())).impact(10).build(),
                FilterConfig.builder().id(53).title("Description = Title").importance(NOTICE).filter(isValidHtmlAndStatusCode().and(fp -> fp.getDescriptionCount() > 0 && fp.getTitleCount() > 0 && fp.getDescription().equals(fp.getTitle()))).build(),
                FilterConfig.builder().id(54).title("Slow loading").importance(WARNING).filter(fp -> fp.isProxyEnabled() && fp.getLoadTime() > 2000).build(),
                FilterConfig.builder().id(55).title("404 Page").importance(ERROR).filter(fp -> fp.getStatusCode() == 404).impact(100).build(),
                FilterConfig.builder().id(56).title("Timeout").importance(ERROR).filter(fp -> fp.getStatusCode() == 504).impact(100).build(),
                FilterConfig.builder().id(58).title("Canonical from HTTP to HTTPS").importance(NOTICE).filter(fp -> fp.getStatusCode() == 200 && isHttp(fp.getUrl()) && !isHttp(fp.getCanonical())).build(),
                FilterConfig.builder().id(59).title("Canonical from HTTPS to HTTP").importance(NOTICE).filter(fp -> fp.getStatusCode() == 200 && !isHttp(fp.getUrl()) && isHttp(fp.getCanonical())).build(),
                FilterConfig.builder().id(62).title("HTTPS to HTTP redirect").importance(WARNING).filter(fp -> {
                    if (fp.getStatusCode() < 300 || fp.getStatusCode() >= 400) {
                        return false;
                    }
                    boolean http = isHttp(fp.getUrl());

                    return !http && isHttp(fp.getNodes().iterator().next().getUrl());
                }).impact(15).build(),
                FilterConfig.builder().id(63).title("HTTPS page links to HTTP image").importance(WARNING).filter(fp -> httpsHasHttpContentLink(fp.getUrl(), fp.getNodes(), NodeType.IMAGE)).impact(3).build(),
                FilterConfig.builder().id(64).title("HTTPS page links to HTTP JavaScript").importance(WARNING).filter(fp -> httpsHasHttpContentLink(fp.getUrl(), fp.getNodes(), NodeType.JS)).impact(3).build(),
                FilterConfig.builder().id(65).title("HTTPS page links to HTTP CSS").importance(WARNING).filter(fp -> false).impact(3).build(),
                FilterConfig.builder().id(66).title("Page has redirected image").importance(WARNING).filter(fp -> false).impact(3).build(),
                FilterConfig.builder().id(67).title("Page has redirected JavaScript").importance(WARNING).filter(fp -> false).impact(3).build(),
                FilterConfig.builder().id(68).title("Page has redirected CSS").importance(WARNING).filter(fp -> false).impact(3).build(),
                FilterConfig.builder().id(70).title("Page has broken image").importance(WARNING).filter(fp -> false).impact(6).build(),
                FilterConfig.builder().id(71).title("Page has broken JavaScript").importance(WARNING).filter(fp -> false).impact(6).build(),
                FilterConfig.builder().id(72).title("Page has broken CSS").importance(WARNING).filter(fp -> false).impact(6).build(),
                FilterConfig.builder().id(73).title("Broken page").importance(ERROR).filter(fp -> fp.getStatusCode() >= 400 && fp.getStatusCode() < 600).impact(70).build(),
                FilterConfig.builder().id(74).title("HTTPS/HTTP mixed internal links").importance(WARNING).filter(fp -> hasMixedContentLink(fp.getUrl(), fp.getNodes())).impact(5).build(),
                FilterConfig.builder().id(75).title("Favicon is not present").importance(NOTICE).filter(isValidHtmlAndStatusCode().and(fp -> fp.getFavicon().isEmpty())).impact(3).build(),
                FilterConfig.builder().id(77).title("Redirect").importance(NOTICE).filter(fp -> fp.getStatusCode() >= 300 && fp.getStatusCode() < 400).build()
        );
    }

    static {
        allLinkFiltersList = Arrays.asList(
                LinkFilterConfig.builder().id(10).where("%s.status_code = 301").build(),
                LinkFilterConfig.builder().id(11).where("%s.status_code != 200").build(),
                LinkFilterConfig.builder().id(21).where("%s.status_code = 200").build(),
                LinkFilterConfig.builder().id(23).where("%s.status_code > 301 and %s.status_code < 400").build(),
                LinkFilterConfig.builder().id(24).where("%s.status_code >= 400 and %s.status_code < 500").build(),
                LinkFilterConfig.builder().id(25).where("%s.status_code >= 500 and %s.status_code < 600").build(),
                LinkFilterConfig.builder().id(51).where("%s.status_code = 302").build(),
                LinkFilterConfig.builder().id(73).where("%s.status_code >= 400 and %s.status_code < 600").build(),
                LinkFilterConfig.builder().id(77).where("%s.status_code >= 300 and %s.status_code < 400").build()
        );
    }

    private static boolean isHttp(String url) {
        return url.startsWith("http://");
    }

    private static boolean httpsHasHttpContentLink(String url, Set<Node> nodes, NodeType nodeType) {
        boolean http = isHttp(url);

        if (!http) {
            return false;
        }

        for (Node node : nodes) {
            if (!node.getType().equals(nodeType)) {
                return false;
            }
            boolean linkHttp = isHttp(node.getUrl());

            if (linkHttp) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasMixedContentLink(String url, Set<Node> nodes) {
        boolean http = isHttp(url);

        for (Node node : nodes) {
            if (!node.getType().equals(NodeType.INTERNAL)) {
                continue;
            }
            boolean linkHttp = isHttp(node.getUrl());

            if (http && !linkHttp) {
                return true;
            } else if (!http && linkHttp) {
                return true;
            }
        }

        return false;
    }

    private static String getMetadataParamNotEmpty(String param) {
        return String.format("length(%s) > 0 ", getMetadataParam(param));
    }

    static {
        allFiltersList = new ArrayList<>();

        allFiltersList.addAll(issuesFilterList);
        allFiltersList.addAll(dbFilterList);
        allFiltersList.addAll(predicateFilterList);

        impactFilterList = allFiltersList.stream().filter(f -> f.impact > 0).collect(Collectors.toList());

        filters = allFiltersList.stream().collect(Collectors.toMap(filter -> filter.id, filter -> filter));
        linkFilters = allLinkFiltersList.stream().collect(Collectors.toMap(filter -> filter.id, filter -> filter));

        issueIdToFilterIds = new HashMap<FilterImportance, List<Integer>>() {{
            put(ERROR, allFiltersList.stream().filter(f -> f.importance.equals(ERROR)).map(FilterConfig::getId).collect(Collectors.toList()));
            put(WARNING, allFiltersList.stream().filter(f -> f.importance.equals(WARNING)).map(FilterConfig::getId).collect(Collectors.toList()));
            put(NOTICE, allFiltersList.stream().filter(f -> f.importance.equals(NOTICE)).map(FilterConfig::getId).collect(Collectors.toList()));
        }};
    }
}