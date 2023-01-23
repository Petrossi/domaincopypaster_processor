package com.domainsurvey.crawler.service.dao.impl;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.type.CrawlingStatus;
import com.domainsurvey.crawler.service.dao.DomainService;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.service.dao.repository.DomainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class DomainServiceImpl implements DomainService {

    private final DomainRepository domainRepository;
    private final QueryExecutor queryExecutor;

    @Override
    public void save(Domain domain) {
        domainRepository.save(domain);
    }

    @Override
    public Optional<Domain> find(String id) {
        return domainRepository.findById(id);
    }

    @Override
    public Optional<Domain> findDeleted() {
        return domainRepository.findFirstByStatus(CrawlingStatus.DELETED);
    }

    @Override
    public Domain getRecentlyCrawledDomain(Domain domain) {
        return null;
    }

    @Override
    public List<Domain> findByCrawlingStatus(CrawlingStatus crawlingStatus) {
        return domainRepository.findAllByStatus(crawlingStatus);
    }

    @Override
    public void addToCrawling(Domain domain) {

    }

    @Override
    public Optional<Domain> findFirstByCrawlingStatus(CrawlingStatus crawlingStatus) {
        return domainRepository.findFirstByStatus(crawlingStatus);
    }

    @Override
    public Optional<Domain> getOneNewDomainForCrawling() {
        String sql = "select domain_table.id\n" +
                "from (\n" +
                "    SELECT DISTINCT ON (d.host) d.*\n" +
                "      FROM domain d\n" +
                "      WHERE d.status = " + CrawlingStatus.QUEUE + "\n" +
                "      group by d.id, d.host\n" +
                "    ) as domain_table\n" +
                "inner join config c on domain_table.config_id = c.id\n" +
                "left join domain_crawling_info info on info.id = domain_table.final_crawling_info_id\n" +
                "where not exists(\n" +
                "        select *\n" +
                "        from domain d_in_progress\n" +
                "        where d_in_progress.host = domain_table.host\n" +
                "          and d_in_progress.status = " + CrawlingStatus.CRAWLING + "\n" +
                ")\n" +
                "\n" +
                "order by info.crawling_finished_timestamp asc, domain_table.priority asc, c.pages_limit desc\n" +
                "limit 1";

        return domainRepository.findById(queryExecutor.queryForString(sql));
    }

    @Override
    public void deleteById(String id) {
        log.info("deleteById: {}", id);

        domainRepository.deleteById(id);
    }
}