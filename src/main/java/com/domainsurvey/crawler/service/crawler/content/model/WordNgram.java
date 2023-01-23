package com.domainsurvey.crawler.service.crawler.content.model;

public class WordNgram {
    public int wordCount;
    public String content;
    public int exactTotal;
    public int notExactTotal;

    public WordNgram(String content) {
        this.wordCount = 1;
        this.content = content;
        this.exactTotal = 1;
    }

    public WordNgram(int wordCount, String content, int exactTotal, int notExactTotal) {
        this.wordCount = wordCount;
        this.content = content;
        this.exactTotal = exactTotal;
        this.notExactTotal = notExactTotal;
    }

    @Override
    public String toString() {
        return String.format("%s : %s: %s", content, exactTotal, notExactTotal);
    }
}