package com.domainsurvey.crawler.service.crawler.content.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.nodes.Element;

public class TreeNode {

    private String tag = "";
    private String text = "";
    private List<TreeNode> children = new ArrayList<>();

    public void addNode(TreeNode treeNode) {
        children.add(treeNode);
    }

    public TreeNode(Element currentElement) {
        init(currentElement, "");
    }

    public TreeNode(Element currentElement, String text) {
        init(currentElement, text);
    }

    private void init(Element currentElement, String text) {
        this.tag = currentElement.tag().getName();

        List<Element> elements = currentElement.children();

        if (elements.isEmpty()) {
            if (!text.isEmpty()) {
                this.text = text;
            }
        } else {
            for (Element element : elements) {
                addNode(new TreeNode(element, element.text()));
            }

            filterChildren();

            if (children.size() == 1) {
                setNodeData(children.get(0));
            }
        }
    }

    private void filterChildren() {
        children = children.stream().filter(n -> {
            if (!n.children.isEmpty()) {
                return true;
            }

            List<String> words = Ngrams.sanitiseTextAndWords(n.text.replaceAll("\\d", "")).stream().filter(w -> w.length() > 2).collect(Collectors.toList());

            return words.size() > 1;
        }).collect(Collectors.toList());
    }

    private void setNodeData(TreeNode nodeData) {
        if (!this.tag.equals("a")) {
            this.tag = nodeData.tag;
        }
        this.text = nodeData.text;
        this.children = nodeData.children;
    }

    public String toTextContent() {
        String separator = "\n";
        if (children.isEmpty()) {
            return text;
        } else {
            return this.children
                    .stream()
                    .map(TreeNode::toTextContent)
                    .collect(Collectors.joining(separator));
        }
    }

    public String toHtmlContent() {
        String content = "<" + tag + ">";

        if (this.children.isEmpty()) {
            content += text;
        } else {
            String childContent = this.children
                    .stream()
                    .map(TreeNode::toHtmlContent)
                    .collect(Collectors.joining(""));

            content += childContent;
        }

        content += "</" + tag + ">";

        return content;
    }
}