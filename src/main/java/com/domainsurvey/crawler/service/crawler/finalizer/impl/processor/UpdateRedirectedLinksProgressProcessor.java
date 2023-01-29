package com.domainsurvey.crawler.service.crawler.finalizer.impl.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.type.FinalizerStatus;
import com.domainsurvey.crawler.service.crawler.finalizer.CrawlingFinalizerProcessor;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.service.table.TableService;
import com.domainsurvey.crawler.service.table.type.SchemaType;
import com.domainsurvey.crawler.service.table.type.TableType;

@Service
@RequiredArgsConstructor
@Log4j2
public class UpdateRedirectedLinksProgressProcessor implements CrawlingFinalizerProcessor {

    private final QueryExecutor queryExecutor;

    public void process(Domain domain) {
        log.info("start: " + domain.getId());
        String pageTableTitle = TableService.getFullTableName(domain.getId(), SchemaType.PROCESS, TableType.PAGE);
        String nodeTableTitle = TableService.getFullTableName(domain.getId(), SchemaType.PROCESS, TableType.NODE);

        String template =
                "update NODE_TABLE pt\n" +
                        "set redirected_links = (\n" +
                        "    WITH RECURSIVE redirects AS (\n" +
                        "        SELECT data.id, data.location, data.code, data.url, 1 AS index\n" +
                        "        from (\n" +
                        "                 select n.id                                            as id,\n" +
                        "                        n,url                                           as url,\n" +
                        "                        p.status_code                                   as code,\n" +
                        "                        (p.saved_meta_data ->> 'crc32Location')::bigint as location\n" +
                        "                 from NODE_TABLE n\n" +
                        "                          left join PAGE_TABLE p on n.id = p.id\n" +
                        "             ) as data\n" +
                        "        where data.id = n_update.id\n" +
                        "        UNION ALL\n" +
                        "        SELECT data.id, data.location, data.code, data.url, redirects.index + 1 AS index\n" +
                        "        from (\n" +
                        "                 select n.id                                            as id,\n" +
                        "                        n.url                                           as url,\n" +
                        "                        p.status_code                                   as code,\n" +
                        "                        (p.saved_meta_data ->> 'crc32Location')::bigint as location\n" +
                        "                 from NODE_TABLE n\n" +
                        "                          left join PAGE_TABLE p on n.id = p.id\n" +
                        "             ) as data\n" +
                        "                 JOIN redirects\n" +
                        "                      ON redirects.location = data.id\n" +
                        "    )\n" +
                        "    SELECT json_agg(to_json(redirects.*))\n" +
                        "    FROM redirects\n" +
                        ")\n" +
                        "from NODE_TABLE n_update\n" +
                        "         inner join PAGE_TABLE p_update on n_update.id = p_update.id\n" +
                        "where  pt.id = n_update.id and p_update.status_code >= 300\n" +
                        "  and p_update.status_code < 400;";

        String sql = template
                .replaceAll("NODE_TABLE", nodeTableTitle)
                .replaceAll("PAGE_TABLE", pageTableTitle);

        queryExecutor.executeQuery(sql);

        log.info("finished : " + domain.getId());
    }

    @Override
    public FinalizerStatus getNextStatus() {
        return FinalizerStatus.RESOURCE_DOWNLOADER;
    }
}