package com.domainsurvey.crawler.utils;

import org.apache.commons.lang3.StringUtils;

import java.net.IDN;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.domainsurvey.crawler.utils.Utils.convertFomDBValidValueURL;
import static com.domainsurvey.crawler.utils.Utils.getDomainHostFromURL;

public class UrlHelper {
    public static String getValidUrl(String url, String pageToProcessUrl) {
        return getValidUrl(url, pageToProcessUrl, true);
    }

    public static String getDomainFromURL(String url) {
        try {
            return new URL(url).getHost();
        } catch (MalformedURLException e) {
            if (url.startsWith("http:")) {
                return url;
            } else {
                return getDomainFromURL("http://" + url);
            }
        }
    }

    public static String getValidUrl(String url, String pageToProcessUrl, boolean retry) {
        if (pageToProcessUrl.equals("")) {
            return pageToProcessUrl;
        }
        pageToProcessUrl = convertFomDBValidValueURL(pageToProcessUrl);

        String pageToProcessUrlProtocol = pageToProcessUrl.split("://")[0];
        String pageToProcessUrlDomain = pageToProcessUrlProtocol + "://" + getDomainFromURL(pageToProcessUrl);

        try {
            if (url.startsWith("#")) {//обрабатываем вариант <a href="#/rwebg"> or <a href="#">  returns current.url/rwebg
                url = pageToProcessUrl;
            }

            if (url.contains("#")) {
                url = url.split("#")[0];
            }

            if (url.startsWith("../")) {//обрабатываем вариант <a href="../path/path" >
                url = resolveRelativePath(pageToProcessUrl, url);
            }
            if (url.startsWith("//")) {//обрабатываем вариант <href="//urls.efrgt"> нужно подставить протокол
                url = pageToProcessUrlProtocol + ":" + url;
            } else if (url.startsWith("/")) {//обрабатываем вариант <a href="/erget/rgetr"> returns current.url/erget/rgetr
                url = pageToProcessUrlDomain + url;
            } else if (url.startsWith("?")) {//обрабатываем вариант для current.com/ <a href="?page=3"> returns current.com/ewret?345g
                if (pageToProcessUrl.contains("?")) {
                    String urlWithoutQuery = pageToProcessUrl.split("\\?")[0];

                    url = urlWithoutQuery + url;
                } else {
                    String pageToProcessUrlPath = pageToProcessUrl.replace(pageToProcessUrlDomain, "");

                    if (!Objects.equals(pageToProcessUrlPath, "")) {
                        if (!pageToProcessUrlPath.startsWith("/")) {
                            pageToProcessUrlPath = "/" + pageToProcessUrlPath;
                        }
                        if (!pageToProcessUrlPath.endsWith("/")) {
                            pageToProcessUrlPath = pageToProcessUrlPath + "/";
                        }
                    }

                    if (!Objects.equals(pageToProcessUrlPath, "") && !pageToProcessUrlPath.startsWith("/")) {
                        pageToProcessUrlPath = "/" + pageToProcessUrlPath;
                    }

                    url = pageToProcessUrlDomain + pageToProcessUrlPath + url;
                }
            }

            if (!url.startsWith("http:") && !url.startsWith("https:") && !url.startsWith("https%3A%2F%2F") && !url.startsWith("http%3A%2F%2F")) {//обрабатываем вариант <a href="page/ewreg"> returns current.com/ewret/ewreq
                if (url.contains(":") && url.contains("@")) {//щьрфбатываем ваоиант если  <a href="malito:suppot@fackyou.com"> returns malito:suppot@fackyou.com <a href="rgty/rgtrh"> returns http:domian.com/rgty/rgtrh
                    return url;
                }

                String tmpUrl = pageToProcessUrl.replace(pageToProcessUrlDomain, "");
                List<String> parts = Arrays.asList(url.replace("//", "").split("/"));

                String lastPath = parts.size() > 0 ? parts.get(parts.size() - 1) : "";

                if (lastPath.contains(".")) {
                    url = resolveUrlWithFile(tmpUrl, url, pageToProcessUrlDomain);
                } else {
                    url = resolveHardRelative(url, pageToProcessUrl, pageToProcessUrlDomain);
                }
            }
            if (url.endsWith("//")) {
                url = url.substring(0, url.length() - 1);
            }
            url = url.replace("'", "''");
            url = url.replace(":80", "");//default for http
            url = url.replace(":443", "");//default for https

            if (url.contains("%")) {
                try {
                    url = convertFomDBValidValueURL(url);
                } catch (IllegalArgumentException ignored) {
                }
            }
        } catch (MalformedURLException e) {//if urls contains invalid char https://preply.com/slov`yanoserbsk/repetitory--frantsuzskogo needs ti encoe
            if (retry) {
                try {
                    String encodedURl = convertFomDBValidValueURL(url);

                    String encodedValidUrl = getValidUrl(encodedURl, pageToProcessUrl, false);

                    url = URLDecoder.decode(encodedValidUrl, "UTF-8");
                } catch (Exception ignored) {
                }
            }
        }

        url = url.trim();

        return url;
    }

    private static String getFileUrl(String url) {
        String validUrl = url;
        if (validUrl.startsWith("http")) {
            String urlProtocol = validUrl.split("://")[0];
            String urlDomain = urlProtocol + "://" + validUrl.split("://")[1].split("/")[0];
            validUrl = validUrl.replace(urlDomain, "");
        }
        if (!validUrl.contains(".")) {
            return url;
        }
        List<String> tmpUrlParts = Arrays.asList(validUrl.split("[.]"));

        String baseUrlWithoutType = tmpUrlParts.get(0);
        String type = tmpUrlParts.get(1).split("/")[0];

        return baseUrlWithoutType + "." + type;
    }

    private static String resolveUrlWithFile(String path, String url, String domainUrl) throws MalformedURLException {
        String fileOfPath = getFileUrl(path);
        String fileOfUrl = getFileUrl(url);

        return resolveHardRelative(fileOfPath, fileOfUrl, domainUrl);
    }

    private static String resolveHardRelative(String url, String baseUrl, String domainUrl) throws MalformedURLException {
        int countOfSlashes = StringUtils.countMatches(url, "/");

        if (countOfSlashes == 0) {
            return domainUrl + "/" + url;
        }

        String prefix = StringUtils.repeat("../", countOfSlashes);

        String relativeUrl = prefix + baseUrl;

        relativeUrl = relativeUrl.replace("//", "/");

        return resolveRelativePath(domainUrl + url, relativeUrl);
    }

    private static String resolveRelativePath(String basicUrl, String url) throws MalformedURLException {
        if (!basicUrl.endsWith("/")) {
            basicUrl += "/";
        }
        URL urlParts = new URL(basicUrl);

        String path = urlParts.getPath();

        List<String> paths = new ArrayList<>(Arrays.asList(path.split("/")));
        while (true) {
            if (url.startsWith("../")) {
                if (paths.isEmpty()) {
                    break;
                }

                paths.remove(paths.size() - 1);
                url = url.replaceFirst("../", "");
            } else {
                break;
            }
        }
        url = url.replaceAll("\\.\\./", "");

        if (!paths.isEmpty()) {

            String originPath = String.join("/", paths);

            if (!url.endsWith("/")) {
                originPath = originPath.concat("/");
            }
            url = originPath + url;
        }

        String domainUrl = String.format("%s://%s", urlParts.getProtocol(), urlParts.getHost());
        if (!url.startsWith("/")) {
            domainUrl = domainUrl + "/";
        }


        String finalUrl = domainUrl + url;

        finalUrl = finalUrl.replace("////", "//");

        return finalUrl;
    }

    public static boolean isUrlValidForDomain(String url, String domainUrl) {
        String domainFullUrl = domainUrl;
        String fullUrl = url;
        url = url.replace("http://", "");
        url = url.replace("https://", "");
        url = url.replace("www.", "");
        url = url.toLowerCase();

        domainUrl = domainUrl.replace("http://", "");
        domainUrl = domainUrl.replace("https://", "");
        domainUrl = domainUrl.replace("www.", "");
        domainUrl = domainUrl.toLowerCase();

        boolean isEmpty = url.equals("");
        boolean isJSAction = url.contains("javascript:void");

        String domainHost = getDomainHostFromURL(domainUrl);
        String urlHost = getDomainHostFromURL(url);
        String encodedUrl = encodeIdn(fullUrl)
                .replace("http://", "")
                .replace("https://", "")
                .replace("www.", "");
        String encodedDomainUrl = encodeIdn(domainFullUrl)
                .replace("http://", "")
                .replace("https://", "")
                .replace("www.", "");
        return !(isEmpty || isJSAction) && (url.startsWith("/") || url.startsWith(".")
                || (url.startsWith(domainUrl) && domainHost.equals(urlHost)))
                || encodedDomainUrl.startsWith(urlHost)
                || domainHost.startsWith(encodedUrl);
    }

    public static String encodeIdn(String url) {
        String resultUrl = url;
        try {
            URL urlParts = new URL(url);
            String urlPath = url.replaceAll(urlParts.getProtocol() + "://" + urlParts.getHost(), "");
            String encodedUrl = urlParts.getProtocol() + "://" + IDN.toASCII(urlParts.getHost());
            resultUrl = encodedUrl + urlPath;
        } catch (Exception ignored) {
        }

        return resultUrl;
    }
}