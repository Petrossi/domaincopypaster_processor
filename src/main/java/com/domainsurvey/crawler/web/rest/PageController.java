package com.domainsurvey.crawler.web.rest;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.link.Node;
import com.domainsurvey.crawler.model.type.NodeType;
import com.domainsurvey.crawler.service.PageCacheManager;
import com.domainsurvey.crawler.service.PageModifier;
import com.domainsurvey.crawler.service.dao.DomainService;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.service.table.TableService;
import com.domainsurvey.crawler.service.table.type.SchemaType;
import com.domainsurvey.crawler.service.table.type.TableType;
import com.domainsurvey.crawler.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.domainsurvey.crawler.utils.Utils.getDomainHostFromURL;

@CrossOrigin()
@RestController
@RequiredArgsConstructor
public class PageController {

    private final DomainService domainService;
    private final PageCacheManager pageCacheManager;
    private final PageModifier pageModifier;
    private final QueryExecutor queryExecutor;

    private Domain getDomainByHost(String host) {
        host = getDomainHostFromURL(host);

        host = host.replace(".localhost", "");

        return domainService.findByHost(host).get();
    }

    @GetMapping(value = "/", produces = {MediaType.TEXT_HTML_VALUE})
    @ResponseBody
    public String main(@RequestHeader String host) throws Exception {
        var domain = getDomainByHost(host);

        String nodeTable = TableService.getFullTableName(domain.getId(), SchemaType.FINAL, TableType.NODE);

        String sql = String.format("SELECT id, url, type FROM %s p where depth = 0", nodeTable);

        var node = queryExecutor.queryForObject(sql, (rs, var1) -> Node.builder()
                .id(rs.getLong("id"))
                .url(rs.getString("url"))
                .type(NodeType.fromValue(rs.getByte("type"))).build());

        return pageModifier.build(getDomainByHost(host), node);
    }

    @GetMapping(value = "/{pageId}", produces = {MediaType.TEXT_HTML_VALUE})
    @ResponseBody
    public String getValidFirstDomainUrl(@RequestHeader String host, @PathVariable long pageId) throws Exception {
        return pageModifier.build(getDomainByHost(host), pageId);
    }

    @GetMapping(value = "/js/{pageId}")
    @ResponseBody
    public void js(@RequestHeader String host, @PathVariable long pageId, HttpServletResponse response) throws Exception {
        download(getDomainByHost(host), pageId, response);
    }

    @GetMapping(value = "/css/{pageId}")
    @ResponseBody
    public void css(@RequestHeader String host, @PathVariable long pageId, HttpServletResponse response) throws Exception {
        download(getDomainByHost(host), pageId, response);
    }

    @GetMapping(value = "/image/{pageId}")
    @ResponseBody
    public void image(@RequestHeader String host, @PathVariable long pageId, HttpServletResponse response) throws Exception {
        download(getDomainByHost(host), pageId, response);
    }

    public void download(Domain domain, long pageId, HttpServletResponse response) throws Exception {
        var filePath = pageCacheManager.getFullPathForUrl(Utils.getCRC32(domain.getUrl()), pageId);

        String mimeType = Files.probeContentType(Path.of(filePath));

        var file = Paths.get(filePath);

        response.setContentType(mimeType);

        Files.copy(file, response.getOutputStream());
        response.getOutputStream().flush();
    }
}