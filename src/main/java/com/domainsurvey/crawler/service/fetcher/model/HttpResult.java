package com.domainsurvey.crawler.service.fetcher.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.json.JSONObject;
import com.domainsurvey.crawler.utils.Utils;

public class HttpResult implements Serializable {

    public short httpStatusCode;
    public String url;
    public String html;
    public String proxy = "";
    public String location;
    public String charsetStr = "";
    public String contentType = "";
    public String serviceType = "";
    public long contentLength;
    public List<String> headers = new ArrayList<>();
    public long loadTime = 0;
    public long realTime = 0;
    public void setHeaders(HttpResponse response) {
        headers = Arrays.stream(response.getAllHeaders())
                .map(header -> header.getName() + " : " + header.getValue())
                .collect(Collectors.toList())
        ;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("url", url);
        jsonObject.put("httpStatusCode", httpStatusCode);
        if (!proxy.isEmpty()) {
            jsonObject.put("proxy", proxy);
        }
        jsonObject.put("html", Utils.limit(html, 500));
        jsonObject.put("location", location);
        jsonObject.put("charsetStr", charsetStr);
        jsonObject.put("contentType", contentType);
        jsonObject.put("contentLength", contentLength);

        return jsonObject.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HttpResult)) return false;
        HttpResult that = (HttpResult) o;
        return httpStatusCode == that.httpStatusCode &&
                contentLength == that.contentLength &&
                loadTime == that.loadTime &&
                Objects.equals(url, that.url) &&
                Objects.equals(html, that.html) &&
                Objects.equals(proxy, that.proxy) &&
                Objects.equals(location, that.location) &&
                Objects.equals(charsetStr, that.charsetStr) &&
                Objects.equals(contentType, that.contentType) &&
                Objects.equals(serviceType, that.serviceType) &&
                Objects.equals(headers, that.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(httpStatusCode, url, html, proxy, location, charsetStr, contentType, serviceType, contentLength, headers, loadTime);
    }
}
