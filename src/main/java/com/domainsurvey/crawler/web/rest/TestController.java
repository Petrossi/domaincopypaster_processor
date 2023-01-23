package com.domainsurvey.crawler.web.rest;

import lombok.RequiredArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.InputSource;
import com.domainsurvey.crawler.service.TreeNodeParser;
import com.domainsurvey.crawler.service.crawler.content.model.TreeNode;
import com.domainsurvey.crawler.service.fetcher.FetcherProcessor;
import com.domainsurvey.crawler.service.fetcher.model.HttpConfig;
import com.domainsurvey.crawler.service.fetcher.model.HttpResult;
import com.domainsurvey.crawler.utils.diff.HtmlCleaner;
import com.domainsurvey.crawler.utils.diff.html.HTMLDiffer;
import com.domainsurvey.crawler.utils.diff.html.HtmlSaxDiffOutput;
import com.domainsurvey.crawler.utils.diff.html.TextNodeComparator;
import com.domainsurvey.crawler.utils.diff.html.dom.DomTreeBuilder;

@CrossOrigin()
@RestController
@RequestMapping(value = "/test")
@RequiredArgsConstructor
public class TestController {

    private final TreeNodeParser treeNodeParser;
    private final FetcherProcessor fetcherProcessor;

    @GetMapping(value = "/toHtmlContent", produces = {MediaType.TEXT_HTML_VALUE})
    @ResponseBody
    public String toHtmlContent(@RequestParam(value = "url") String url) throws Exception {
        return treeNodeParser.parseHtmlContentByUrl(url).toHtmlContent();
    }

    @GetMapping(value = "/toTextContent", produces = {MediaType.TEXT_HTML_VALUE})
    @ResponseBody
    public String toTextContent(@RequestParam(value = "url") String url) throws Exception {
        return treeNodeParser.parseHtmlContentByUrl(url).toTextContent();
    }

    @GetMapping(value = "/getHtmlDiff", produces = {MediaType.TEXT_HTML_VALUE})
    @ResponseBody
    public String toTextContent() throws Exception {
        HttpResult result = fetcherProcessor.getPage(HttpConfig.builder().url("https://edubirdie.com/essay-writing-help-online").timeout(5).followRedirect(true).build());

        Document document = Jsoup.parse(result.html);
        String html1 = document.html();

        document.getAllElements().forEach(element -> {
            if (element.children().isEmpty() && !element.text().isEmpty()) {
                element.text(element.text() + "test");
            }
        });

        String html2 = document.html();


        String htmlContent1 = new TreeNode(Jsoup.parse(html1).body()).toHtmlContent();
        String htmlContent2 = new TreeNode(Jsoup.parse(html2).body()).toHtmlContent();

        return htmlContent1 + htmlContent2;
    }

    @GetMapping(value = "/getHtml/{new}", produces = {MediaType.TEXT_HTML_VALUE})
    @ResponseBody
    public String newHtml(@PathVariable(value = "new") boolean isNww) throws Exception {
        String url = "hhttps://neilpatel.com/";
        HttpConfig config = HttpConfig.builder().url(url).timeout(5).followRedirect(true).build();
        HttpResult result = fetcherProcessor.getPage(config);

        if (!isNww) {
            return result.html;
        }

        Document document = Jsoup.parse(result.html);

        document.getAllElements().forEach(element -> {
            if (element.children().isEmpty() && !element.text().isEmpty()) {
                Element sub = element.appendElement("item");
                sub.text("test");

//                element.children().add(new Element(Tag.valueOf("span"), "test"));
//                element.text(element.text() + "test");
            }
        });

        return document.html();
    }

    public static final String CHARSET_ENCODING = StandardCharsets.UTF_8.name();
    public static final String CONTENT_TYPE = "text/html";

    @GetMapping(value = "/getDiff", produces = {MediaType.TEXT_HTML_VALUE})
    @ResponseBody
    public String test() throws Exception {
        String oldHtmlSource = fetcherProcessor.getPage(HttpConfig.builder().url("https://edubirdie.com/essay-writing-help-online").timeout(5).followRedirect(true).build()).html;
        String newHtmlSource = fetcherProcessor.getPage(HttpConfig.builder().url("https://edubirdie.com/college-application-essay-writing").timeout(5).followRedirect(true).build()).html;
        String oldHtml = new TreeNode(Jsoup.parse(oldHtmlSource).body()).toHtmlContent();
        String newHtml = new TreeNode(Jsoup.parse(newHtmlSource).body()).toHtmlContent();

//        oldHtml = oldHtmlSource;
//        newHtml = newHtmlSource;

        OutputStream outputStream = new ByteArrayOutputStream();

        InputStream oldStream = new ByteArrayInputStream(oldHtml.getBytes());
        InputStream newStream = new ByteArrayInputStream(newHtml.getBytes());

        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler result = tf.newTransformerHandler();
        result.getTransformer().setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        result.getTransformer().setOutputProperty(OutputKeys.METHOD, "html");
        result.getTransformer().setOutputProperty(OutputKeys.ENCODING, CHARSET_ENCODING);
        result.setResult(new StreamResult(outputStream));

        Locale locale = Locale.forLanguageTag("en");
        String prefix = "diff";

        HtmlCleaner cleaner = new HtmlCleaner();
        DomTreeBuilder oldHandler = new DomTreeBuilder();

        InputSource oldSource = new InputSource(oldStream);
        oldSource.setEncoding(CHARSET_ENCODING);
        InputSource newSource = new InputSource(newStream);
        newSource.setEncoding(CHARSET_ENCODING);
        cleaner.cleanAndParse(oldSource, oldHandler);

        TextNodeComparator leftComparator = new TextNodeComparator(oldHandler, locale);

        DomTreeBuilder newHandler = new DomTreeBuilder();
        cleaner.cleanAndParse(newSource, newHandler);
        TextNodeComparator rightComparator = new TextNodeComparator(newHandler, locale);

        result.startDocument();
        HtmlSaxDiffOutput output = new HtmlSaxDiffOutput(result, prefix);

        HTMLDiffer differ = new HTMLDiffer(output);
        differ.diff(leftComparator, rightComparator);

        result.endDocument();

        String resultHtml = outputStream.toString();

        return cssToAppend() + resultHtml;
    }

    private String cssToAppend() {
        return "<style type=\"text/css\">/*\n" +
                " * Styles for the Tag Diff\n" +
                " */\n" +
                "span.diff-tag-html {\n" +
                "  font-family: \"Andale Mono\" monospace;\n" +
                "  font-size: 80%;\n" +
                "}\n" +
                "\n" +
                "span.diff-tag-removed {\n" +
                "  font-size: 100%;\n" +
                "  text-decoration: line-through;\n" +
                "  background-color: #fdc6c6; /* light red */\n" +
                "}\n" +
                "\n" +
                "span.diff-tag-added {\n" +
                "  font-size: 100%;\n" +
                "  background-color: #ccffcc; /* light green */\n" +
                "}\n" +
                "\n" +
                "span.diff-tag-conflict {\n" +
                "  font-size: 100%;\n" +
                "  background-color: #f781be; /* light rose */\n" +
                "}\n" +
                "\n" +
                "/*\n" +
                " * Styles for the HTML Diff\n" +
                " */\n" +
                "span.diff-html-added {\n" +
                "  font-size: 100%;\n" +
                "  background-color: #ccffcc; /* light green */\n" +
                "  cursor: pointer;\n" +
                "}\n" +
                "\n" +
                "span.diff-html-removed {\n" +
                "  font-size: 100%;\n" +
                "  text-decoration: line-through;\n" +
                "  background-color: #fdc6c6; /* light red */\n" +
                "    cursor: pointer;\n" +
                "}\n" +
                "\n" +
                "span.diff-html-changed {\n" +
                "  background: url(../images/diffunderline.gif) bottom repeat-x;\n" +
                "  *background-color: #c6c6fd; /* light blue */\n" +
                "  cursor: pointer;\n" +
                "}\n" +
                "\n" +
                "span.diff-html-conflict {\n" +
                "/*  background: url(../images/diffunderline.gif) bottom repeat-x; */\n" +
                "  background-color: #f781be; /* light rose */\n" +
                "}\n" +
                "\n" +
                "span.diff-html-selected {\n" +
                "  background-color: #FF8800; /* light orange */\n" +
                "  cursor: pointer;\n" +
                "}\n" +
                "\n" +
                "span.diff-html-selected img{\n" +
                "   border: 2px solid #FF8800; /* light orange */\n" +
                "}\n" +
                "\n" +
                "span.diff-html-added img{\n" +
                " border: 2px solid #ccffcc;\n" +
                "}\n" +
                "\n" +
                "span.diff-html-removed img{\n" +
                " border: 2px solid #fdc6c6;\n" +
                "}\n" +
                "\n" +
                "span.diff-html-changed img{\n" +
                " border: 2px dotted #000099;\n" +
                " \n" +
                "}\n" +
                "\n" +
                "div.diff-removed-image, div.diff-added-image, div.diff-conflict-image {\n" +
                "  height: 300px;\n" +
                "  width: 200px;  \n" +
                "  position: absolute;\n" +
                "  opacity : 0.55;\n" +
                "  filter: alpha(opacity=55);\n" +
                "  -moz-opacity: 0.55;\n" +
                "}\n" +
                "\n" +
                "div.diff-removed-image, div.diff-added-image, div.diff-conflict-image  {\n" +
                "  margin-top: 2px;\n" +
                "  margin-bottom: 2px;\n" +
                "  margin-right: 2px;\n" +
                "  margin-left: 2px;\n" +
                "}\n" +
                "\n" +
                "div.diff-removed-image {\n" +
                "  background-color: #fdc6c6;\n" +
                "  background-image: url(../images/diffmin.gif);\n" +
                "}\n" +
                "div.diff-added-image {\n" +
                "  background-color: #ccffcc;\n" +
                "  background-image: url(../images/diffplus.gif);\n" +
                "  background-repeat: no-repeat;\n" +
                "}\n" +
                "\n" +
                "div.diff-conflict-image {\n" +
                "  background-color: #f781be;\n" +
                "  background-image: url(../images/diffconflict.gif);\n" +
                "  background-repeat: no-repeat;\n" +
                "}\n" +
                "\n" +
                "img.diff-icon {\n" +
                "  background-color: #FF8800;\n" +
                "  background-image: url(../images/bg_rounded.gif);\n" +
                "  width: 16px;\n" +
                "  height: 16px;\n" +
                "  border: 0px none;\n" +
                "}\n" +
                "\n" +
                "table.diff-tooltip-link, table.diff-tooltip-link-changed {\n" +
                "   width: 100%;\n" +
                "   text-align: center;\n" +
                "   Vertical-align: middle;\n" +
                "}\n" +
                "\n" +
                "table.diff-tooltip-link-changed {\n" +
                "    border-top: thin dashed #000000; \n" +
                "    margin-top: 3px; \n" +
                "    padding-top: 3px\n" +
                "}\n" +
                "td.diff-tooltip-prev {\n" +
                "   text-align: left;\n" +
                "}\n" +
                "\n" +
                "td.diff-tooltip-next {\n" +
                "   text-align: right;\n" +
                "}\n" +
                "\n" +
                "table.diffpage-html-firstlast {\n" +
                "  width: 100%;\n" +
                "  Vertical-align: middle;\n" +
                "}\n" +
                "\n" +
                "div.diff-topbar{\n" +
                " border-bottom: 2px solid #FF8800;\n" +
                " border-left: 1px solid #FF8800;\n" +
                " border-right: 1px solid #FF8800;\n" +
                " background-color: #FFF5F5;\n" +
                "}\n" +
                "\n" +
                "a.diffpage-html-a, a.diffpage-html-a:hover, a.diffpage-html-a:link, a.diffpage-html-a:visited, a.diffpage-html-a:active {\n" +
                "  text-decoration: none;\n" +
                "  color: #FF8800;\n" +
                "}\n" +
                "\n" +
                ".diffpage-html-firstlast a img, .dsydiff-prevnextnav a img {\n" +
                "  vertical-align: middle;\n" +
                "}\n" +
                "\n" +
                "ul.changelist {\n" +
                "  padding-left: 15px;\n" +
                "}\n" +
                "\n" +
                "body{\n" +
                "  margin-top: 0px;\n" +
                "}\n</style>";
    }
}