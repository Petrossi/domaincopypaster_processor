package com.domainsurvey.crawler.service.crawler.importer.model;

import com.domainsurvey.crawler.model.link.Edge;
import com.domainsurvey.crawler.model.link.Node;
import com.domainsurvey.crawler.model.page.Page;
import com.google.code.yanf4j.util.ConcurrentHashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ImportStore {
    public final List<Page> pages = Collections.synchronizedList(new ArrayList<>());
    public final List<Node> nodes = Collections.synchronizedList(new ArrayList<>());
    public final List<Edge> edges = Collections.synchronizedList(new ArrayList<>());

    private final Object lock = new Object();

    public final Set<Long> alreadyImporter = new ConcurrentHashSet<>();

    public void addPage(Page page) {
        if (page.getId() == 0) {
            System.out.println();
        }
        pages.add(page);
    }

    public void addLink(Node node) {
        synchronized (lock) {
            if (node.getId() == 0) {
                System.out.println();
            }
            if (!alreadyImporter.contains(node.getId())) {
                alreadyImporter.add(node.getId());
                nodes.add(node);
            }
        }
    }

    public int getStoresSize() {
        int totalSize;

        synchronized (this) {
            totalSize = pages.size() + nodes.size() + edges.size();
        }
        return totalSize;
    }

    public void clear() {
        pages.clear();
        nodes.clear();
        edges.clear();
    }
}