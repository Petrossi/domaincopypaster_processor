package com.domainsurvey.crawler.service.crawler.importer;

import java.util.List;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.link.Edge;
import com.domainsurvey.crawler.model.link.Node;
import com.domainsurvey.crawler.model.page.Page;

public interface InsertImportService {
    void importPages(List<Page> pages, Domain domain);

    void importLinks(List<Node> nodes, Domain domain);

    void importEdges(List<Edge> edges, Domain domain);
}