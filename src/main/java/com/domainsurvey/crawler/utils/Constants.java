package com.domainsurvey.crawler.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Constants {

    private final List<String> USER_AGENTS = Arrays.asList(
            "Linux / Firefox 29: Mozilla/5.0 (X11; Linux x86_64; rv:29.0) Gecko/20100101 Firefox/29.0",
            "Linux / Chrome 34: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.137 Safari/537.36",
            "Mac / Firefox 29: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:29.0) Gecko/20100101 Firefox/29.0",
            "Mac / Chrome 34: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.137 Safari/537.36",
            "Mac / Safari 7: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/537.75.14",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36",
            "Windows / Firefox 29: Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0",
            "Windows / Chrome 34: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.137 Safari/537.36",
            "Windows / IE 6: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)",
            "Windows / IE 7: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)",
            "Windows / IE 8: Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0)",
            "Windows / IE 9: Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)",
            "Windows / IE 10: Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)",
            "Windows / IE 11: Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko",
            "Android / Firefox 29: Mozilla/5.0 (Android; Mobile; rv:29.0) Gecko/29.0 Firefox/29.0",
            "Android / Chrome 34: Mozilla/5.0 (Linux; Android 4.4.2; Nexus 4 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.114 Mobile Safari/537.36",
            "iOS / Chrome 34: Mozilla/5.0 (iPad; CPU OS 7_0_4 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) CriOS/34.0.1847.18 Mobile/11B554a Safari/9537.53",
            "iOS / Safari 7: Mozilla/5.0 (iPad; CPU OS 7_0_4 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Version/7.0 Mobile/11B554a Safari/9537.53"
    );

    public static final List<Short> BAD_STATUS_CODES = Stream.of(
            0,
            403,
            429,
            503,
            504,
            520,
            529,
            451,
            521
    ).map(Integer::shortValue).collect(Collectors.toList());

    public static final String BAD_STATUS_CODES_STRING = BAD_STATUS_CODES.stream().map(String::valueOf).collect(Collectors.joining(", "));

    public static final String[] NOT_SECURE_PROTOCOLS = {"SSLv2Hello", "SSLv3", "TLSv1.1"};
    public static final String[] SECURE_PROTOCOLS = {"TLSv1", "TLSv1.2"};

    public static final short HTTP_EQUIV_REDIRECT = 309;

    public static List<String> INVALID_TAGS = Arrays.asList(
            "br",
            "select",
            "button",
            "form",
            "nav",
            "header",
            "img",
            "footer"
    );
}