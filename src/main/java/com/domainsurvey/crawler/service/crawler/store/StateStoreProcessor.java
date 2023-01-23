package com.domainsurvey.crawler.service.crawler.store;

import java.util.List;

import com.domainsurvey.crawler.model.link.Node;
import com.domainsurvey.crawler.model.page.Page;
import com.domainsurvey.crawler.service.crawler.model.PageResult;

public interface StateStoreProcessor extends Runnable {

    void start();

    void stop();

    void init();

    void nodeStarted(Node nodeToProcess);

    void pageFinished(PageResult pageResult);

    void logCountersByImportedPages(List<Page> importedPages);
}