package com.domainsurvey.crawler.service.crawler.importer.impl;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.link.Edge;
import com.domainsurvey.crawler.model.link.Node;
import com.domainsurvey.crawler.model.page.Page;
import com.domainsurvey.crawler.service.crawler.importer.InsertImportService;
import com.domainsurvey.crawler.service.crawler.importer.model.ImportStore;
import com.domainsurvey.crawler.service.crawler.processor.CrawlingProcessorService;
import com.domainsurvey.crawler.service.crawler.store.model.CrawlingStore;
import com.domainsurvey.crawler.service.crawler.store.model.StateStore;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.domainsurvey.crawler.utils.Utils.sleepSeconds;

@Service
@Scope("prototype")
@Log4j2
public class ResultImporterProcessor implements Runnable {

    @Autowired
    protected InsertImportService insertImportService;

    protected final Object importLock = new Object();

    protected volatile boolean isRunning = false;
    protected volatile boolean inited = false;
    public volatile long lastImportStart = System.currentTimeMillis();
    public volatile long lastImportFinish = System.currentTimeMillis();

    protected volatile boolean isAddingNew = false;

    protected Thread worker;

    protected final CrawlingProcessorService crawlingProcessorService;
    protected final Domain domain;
    public final ImportStore importStore;
    public final StateStore stateStore;
    public final CrawlingStore crawlingStore;

    public ResultImporterProcessor(CrawlingProcessorService crawlingProcessorService) {
        this.crawlingProcessorService = crawlingProcessorService;
        this.domain = crawlingProcessorService.domain();
        this.importStore = crawlingProcessorService.importStore();
        this.stateStore = crawlingProcessorService.stateStore();
        this.crawlingStore = crawlingProcessorService.crawlingStore();
    }

    protected void init() {
        worker = new Thread(this);

        worker.start();
    }

    public void tryToImportEmpty() {
        tryToImportAll(true);
    }

    public void tryToImportAll(boolean forceInsert) {
        synchronized (importLock) {
            lastImportStart = System.currentTimeMillis();

            isAddingNew = true;

            int totalSize = doImport(forceInsert);

            isAddingNew = false;

            lastImportFinish = System.currentTimeMillis();

            long importingTimeDiff = importingTimeDiff();
            if (totalSize > 0) {
                double timePerLink = (double) importingTimeDiff / (double) totalSize;
                String timePerLinkFormatted = new DecimalFormat("#.##").format(timePerLink);

                String message = String.format(
                        "%s, size: %s | t1: %s | t2: %s | force: %s",
                        domain.getId(), totalSize, importingTimeDiff, timePerLinkFormatted, forceInsert
                );

                log.debug(message);
            }
        }
    }

    private int doImport(boolean forceInsert) {
        List<Page> currentPageResultToImport;
        List<Node> nodes;
        List<Edge> edges;

        int totalSize;

        synchronized (importStore) {
            totalSize = importStore.getStoresSize();
            if (totalSize == 0 || !(forceInsert || totalSize > 30000)) {
                return 0;
            }

            currentPageResultToImport = new ArrayList<>(importStore.pages);
            nodes = new ArrayList<>(importStore.nodes);
            edges = new ArrayList<>(importStore.edges);

            importStore.clear();
        }

        try {
            insertImportService.importPages(currentPageResultToImport, domain);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("error importPageIntoProcessTable " + e.getMessage());
        }

        if (!nodes.isEmpty()) {
            try {
                insertImportService.importLinks(nodes, domain);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("error importLinks " + e.getMessage());
            }
        }

        if (!edges.isEmpty()) {
            try {
                insertImportService.importEdges(edges, domain);
            } catch (Exception e) {
                log.error("error edges " + e.getMessage());
            }
        }

        crawlingProcessorService.stateStoreProcessor().logCountersByImportedPages(currentPageResultToImport);

        return totalSize;
    }

    public void stop() {
        log.info("stop : {}", domain.getId());

        isRunning = false;

        tryToImportEmpty();
    }

    public void start() {
        if (isRunning) {
            log.info("ResultImporter already running: {}", domain.getId());
            return;
        }
        isRunning = true;

        if (!inited) {
            init();

            inited = true;
        }
    }

    protected long importingTimeDiff() {
        return (lastImportFinish - lastImportStart);
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                if (!isAddingNew) {
                    tryToImportEmpty();
                }
                sleepSeconds(3);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }
}