package com.domainsurvey.crawler.service;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.link.Node;
import com.domainsurvey.crawler.service.crawler.model.PageResult;
import com.domainsurvey.crawler.service.dao.painator.link.InternalLinkPaginator;
import com.domainsurvey.crawler.utils.Utils;
import com.domainsurvey.crawler.web.ws.model.request.message.DomainPaginationRequest;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Log4j2
public class PageModifier {

    @Autowired
    private PageCacheManager pageCacheManager;

    @Autowired
    private InternalLinkPaginator internalLinkPaginator;

    public String build(Domain domain, Node node) throws Exception {
        DomainPaginationRequest request = new DomainPaginationRequest();
        request.setId(domain.getId());
        
        request.setAdditionalData(Map.of("id", String.valueOf(node.getId())));
        request.setPageSize(10000);

        var list = internalLinkPaginator.list(request).getData();

        var httpResult = pageCacheManager.getPage(Utils.getCRC32(domain.getUrl()), node.getId());

        PageResult pageResult = PageResult.builder()
                .httpResult(httpResult)
                .linkToProcess(node)
                .domain(domain)
                .build();

        var html = httpResult.html;

        var doc = Jsoup.parse(html);

        var allLinks = doc.select("a");

        for (Element element : allLinks) {
            String originUrl = element.attr("href").replace(" ", "").trim();
            var valid = pageResult.originalToValid.get(originUrl);

            var linkNode = list.stream().filter(n -> n.getUrl().equals(valid)).findFirst();

            linkNode.ifPresent(linkData -> element.attr("href", "linkto/" + linkData.getId()));
        }

        doc.select("script").remove();
        doc.select("noscript").remove();
        doc.select("style").remove();

        return doc.html();
    }
}    
