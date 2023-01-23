package com.domainsurvey.crawler.service.crawler.store.model;

import lombok.SneakyThrows;

import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import com.domainsurvey.crawler.model.link.Node;
import com.domainsurvey.crawler.model.type.NodeType;
import com.google.code.yanf4j.util.ConcurrentHashSet;

public class CrawlingStore {
    private final Set<Long> alreadyQueued = new ConcurrentHashSet<>();
    private BlockingQueue<Node> queue = new LinkedBlockingDeque<>(10000);

    @SneakyThrows
    public void putToQueue(Node nodeToProcess) {
        queue.put(nodeToProcess);
    }

    @SneakyThrows
    public Node pullFromQueue() {
        return queue.poll();
    }

    public int queueSize() {
        return queue.size();
    }

    public int queuePageSize() {
        return (int) queue.stream().filter(n -> n.getType().equals(NodeType.INTERNAL)).count();
    }

    public void alreadyQueuedAdd(List<Long> data) {
        alreadyQueued.addAll(data);
    }

    public boolean alreadyQueuedAdd(Long data) {
        return alreadyQueued.add(data);
    }

    public boolean alreadyQueuedContains(Long data) {
        return alreadyQueued.contains(data);
    }

    public void clearQueue() {
        queue.clear();
    }
}