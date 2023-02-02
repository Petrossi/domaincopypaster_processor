package com.domainsurvey.crawler.service;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.link.Node;
import com.domainsurvey.crawler.model.type.NodeType;
import com.domainsurvey.crawler.service.crawler.model.PageResult;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.service.dao.painator.link.InternalLinkPaginator;
import com.domainsurvey.crawler.service.table.TableService;
import com.domainsurvey.crawler.service.table.type.SchemaType;
import com.domainsurvey.crawler.service.table.type.TableType;
import com.domainsurvey.crawler.utils.Utils;
import com.domainsurvey.crawler.web.ws.model.request.message.DomainPaginationRequest;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.HashMap;

import static com.domainsurvey.crawler.utils.UrlHelper.getValidUrl;

@Service
@Log4j2
@AllArgsConstructor
public class PageModifier {

    private final PageCacheManager pageCacheManager;
    private final QueryExecutor queryExecutor;
    private final InternalLinkPaginator internalLinkPaginator;

    public String build(Domain domain, long pageId) throws Exception {
        String nodeTable = TableService.getFullTableName(domain.getId(), SchemaType.FINAL, TableType.NODE);

        String sql = String.format("SELECT id, url, type FROM %s p where id = %s", nodeTable, pageId);

        var node = queryExecutor.queryForObject(sql, (rs, var1) -> Node.builder()
                .id(rs.getLong("id"))
                .url(rs.getString("url"))
                .type(NodeType.fromValue(rs.getByte("type"))).build());

        return build(domain, node);
    }

    public String build(Domain domain, Node node) throws Exception {
        DomainPaginationRequest request = new DomainPaginationRequest();
        request.setId(domain.getId());
        var additional = new HashMap<String, String>();
        additional.put("id", String.valueOf(node.getId()));
        request.setAdditionalData(additional);
        request.setPageSize(10000);

        var httpResult = pageCacheManager.getPage(Utils.getCRC32(domain.getUrl()), node.getId());

        PageResult pageResult = PageResult.builder()
                .httpResult(httpResult)
                .linkToProcess(node)
                .domain(domain)
                .build();

        var html = httpResult.html;

        var doc = Jsoup.parse(html);

        replaceResource(NodeType.IMAGE, node, doc, request);
        replaceResource(NodeType.CSS, node, doc, request);
//        replaceResource(NodeType.JS, node, doc, request);

        replaceLinks(doc, pageResult, request);

        doc.select("script").remove();
        doc.select(".preloader").remove();
        doc.select(".loader").remove();
        doc.select("noscript").remove();

        Element head = doc.head();

        doc.body().attr("style", "background-color: blue");

//        doc.select("style").remove();

        return doc.html();
    }

    private void replaceLinks(Document doc, PageResult pageResult, DomainPaginationRequest request) {
        request.getAdditionalData().put("type", String.valueOf(NodeType.INTERNAL.getValue()));

        var list = internalLinkPaginator.list(request).getData();

        var allLinks = doc.select("a");

        log.info("replace {} with {}", allLinks.size(), list.size());

        for (Element element : allLinks) {
            String originUrl = element.attr("href").replace(" ", "").trim();
            var valid = pageResult.originalToValid.get(originUrl);

            var linkNode = list.stream().filter(n -> n.getUrl().equals(valid)).findFirst();

            if (linkNode.isPresent()) {
                element.attr("href", String.format("/%s", linkNode.get().getId()));
            } else {
//                element.remove();
            }
        }
    }

    private void replaceResource(NodeType nodeType, Node node, Document doc, DomainPaginationRequest request) {
        request.getAdditionalData().put("type", String.valueOf(nodeType.getValue()));

        var list = internalLinkPaginator.list(request).getData();

        var elements = switch (nodeType) {
            case IMAGE -> doc.getElementsByTag("img");
            case CSS -> doc.select("link[rel=stylesheet]");
            case JS -> doc.select("script[src]");
            default -> new Elements();
        };

        for (Element element : elements) {
            String originUrl = element.attr("src");

            if (nodeType == NodeType.CSS) {
                originUrl = element.attr("href").split("\\?")[0];
            }
            var valid = getValidUrl(originUrl, node.getUrl());

            var linkNode = list.stream().filter(n -> n.getUrl().equals(valid)).findFirst();

            String finalOriginUrl = originUrl;

            if (nodeType.equals(NodeType.IMAGE)) {
                linkNode.ifPresent(linkData -> element.attr("src", String.format("/image/%s", linkData.getId())));
            } else if (nodeType.equals(NodeType.JS)) {
                linkNode.ifPresent(linkData -> element.attr("src", String.format("/js/%s", linkData.getId())));
            } else if (nodeType.equals(NodeType.CSS)) {
                linkNode.ifPresent(linkData -> element.attr("href", String.format("/css/%s", linkData.getId())));
            }
        }
    }
}
