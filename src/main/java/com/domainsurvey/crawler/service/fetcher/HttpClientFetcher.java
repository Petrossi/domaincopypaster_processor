package com.domainsurvey.crawler.service.fetcher;

import lombok.RequiredArgsConstructor;

import static com.domainsurvey.crawler.utils.Constants.*;
import static com.domainsurvey.crawler.utils.Utils.isRedirected;
import static com.domainsurvey.crawler.utils.Utils.isValidContentType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.exception.BadHttpStatusCodeException;
import com.domainsurvey.crawler.service.fetcher.model.HttpConfig;
import com.domainsurvey.crawler.service.fetcher.model.HttpRequestConfig;
import com.domainsurvey.crawler.service.fetcher.model.HttpResult;
import com.domainsurvey.crawler.service.proxy.ProxyService;

@Service
@RequiredArgsConstructor
public class HttpClientFetcher {

    private final ProxyService proxyService;

    @Value("${crawler.userAgent}")
    protected String userAgent;

    public HttpResult getPage(HttpConfig httpConfig) throws IOException {

        int count = 0;
        HttpResult result;
        IOException lastException = null;

        while (count < httpConfig.maxRetries) {
            count++;

            try {
                result = doGet(HttpConfig
                        .builder()
                        .url(httpConfig.url)
                        .proxy(count > 0 && httpConfig.proxy)
                        .onlyHeaders(httpConfig.onlyHeaders)
                        .followRedirect(httpConfig.followRedirect)
                        .timeout(httpConfig.timeout)
                        .build()
                );

                if (BAD_STATUS_CODES.contains(result.httpStatusCode) && httpConfig.retryAtBadStatusCode) {
                    count++;

                    throw new BadHttpStatusCodeException(result.httpStatusCode);
                }
                return result;
            } catch (IOException e) {
                lastException = e;
            }
        }

        throw lastException;
    }

    private RequestConfig createRequestConfig(HttpConfig httpConfig) {
        int timeout = httpConfig.timeout * 1000;

        return RequestConfig.custom()
                .setSocketTimeout(timeout)
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setRedirectsEnabled(httpConfig.followRedirect)
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .build()
                ;
    }

    private HttpResult execute(HttpConfig httpConfig, HttpRequestBase httpMethod) throws IOException {
        long start = System.currentTimeMillis();
        HttpResult httpResult = new HttpResult();
        httpResult.url = httpConfig.url;
        HttpRequestConfig httpRequestConfig = new HttpRequestConfig(
                createRequestConfig(httpConfig),
                createHandshakeFailureAllHttpClientBuilder()
        );

        if (httpConfig.proxy) {
            HttpHost httpHost = proxyService.getApacheRandomProxy();
            if (httpHost != null) {
                httpResult.proxy = httpHost.toString();
                httpResult.headers.add("Proxy: " + httpHost.toString());
                DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(httpHost);
                httpRequestConfig.httpClientBuilder.setRoutePlanner(routePlanner);
            }
        }

        try {
            httpRequestConfig.httpClient = httpRequestConfig.httpClientBuilder.build();

            httpMethod.setConfig(httpRequestConfig.config);

            httpRequestConfig.response = httpRequestConfig.httpClient.execute(httpMethod);
            httpResult.httpStatusCode = (short) httpRequestConfig.response.getStatusLine().getStatusCode();

            httpResult.initHeaders(httpRequestConfig.response);

            if (isRedirected(httpResult.httpStatusCode)) {
                Header locationHeader = httpRequestConfig.response.getFirstHeader("location");
                if (locationHeader == null) {
                    URL urlParts = new URL(httpConfig.url);
                    httpResult.location = String.format("%s://%s", urlParts.getProtocol(), urlParts.getHost());
                } else {
                    httpResult.location = locationHeader.getValue();
                }
            } else if (httpResult.httpStatusCode >= 200 && httpResult.httpStatusCode < 300) {
                HttpEntity entity = httpRequestConfig.response.getEntity();

                Charset charset = Charset.defaultCharset();

                try {
                    ContentType contentTypeObj = ContentType.getOrDefault(entity);

                    if (contentTypeObj.getCharset() != null) {
                        charset = contentTypeObj.getCharset();
                    }
                    httpResult.contentType = contentTypeObj.getMimeType();
                } catch (Exception ignored) {
                }
                httpResult.charsetStr = charset.displayName();

                if (!httpConfig.onlyHeaders) {
                    if (httpResult.contentType == null) {
                        throw new IOException("content type not found");
                    }
                    httpResult.contentLength = entity.getContentLength();

                    if (isValidContentType(httpConfig.type, httpResult.contentType)) {
                        String html = EntityUtils.toString(entity, charset);
                        if (httpConfig.clearHtml) {
                            html = clearHtml(html);
                        }
                        httpResult.html = html;
                    }
                }
            }
        } finally {
            if (httpMethod != null) {
                httpMethod.releaseConnection();
            }
            if (httpRequestConfig.response != null) {
                httpRequestConfig.response.close();
            }
            if (httpRequestConfig.httpClient != null) {
                httpRequestConfig.httpClient.close();
            }
        }

        long finish = System.currentTimeMillis();
        httpResult.loadTime = (finish - start);
        return httpResult;
    }

    private String clearHtml(String html) {
        try {
            Document document = Jsoup.parse(html);
            Arrays.asList(
                    "script",
                    "iframe",
                    "style",
                    "input",
                    "textarea",
                    "svg",
                    "noscript"
            ).forEach(tag -> document.select(tag).remove());
            html = document.html();
        } catch (Exception ignored) {
        }

        return html;
    }

    private HttpResult doGet(HttpConfig httpConfig) throws IOException {
        HttpGet httpGet = createGet(httpConfig);

        return execute(httpConfig, httpGet);
    }

    private HttpGet createGet(HttpConfig httpConfig) {
        String url = httpConfig.url;
        int timeout = httpConfig.timeout * 1000;

        HttpGet httpGet;
        try {
            httpGet = new HttpGet(url);
        } catch (IllegalArgumentException e) {
            try {
                URL urlToValidate = new URL(url);
                URI uri = new URI(urlToValidate.getProtocol(), urlToValidate.getUserInfo(), urlToValidate.getHost(), urlToValidate.getPort(), urlToValidate.getPath(), urlToValidate.getQuery(), urlToValidate.getRef());
                url = uri.toURL().toString();
            } catch (MalformedURLException | URISyntaxException ignored) {
            }
            httpGet = new HttpGet(url);
        }

        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(timeout)
                .setConnectTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
        httpGet.setConfig(requestConfig);
        return (HttpGet) initHttpRequestBase(httpGet);
    }

    public HttpResult doPost(HttpConfig httpConfig) throws IOException {
        HttpPost httpPost = createPost(httpConfig.url, httpConfig.requestData, httpConfig.contentType);

        return execute(httpConfig, httpPost);
    }

    private HttpPost createPost(String url, String requestData, String contentType) {
        HttpPost httpPost;
        try {
            httpPost = new HttpPost(url);
        } catch (IllegalArgumentException e) {
            try {
                URL urlToValidate = new URL(url);
                URI uri = new URI(urlToValidate.getProtocol(), urlToValidate.getUserInfo(), urlToValidate.getHost(), urlToValidate.getPort(), urlToValidate.getPath(), urlToValidate.getQuery(), urlToValidate.getRef());
                url = uri.toURL().toString();
            } catch (MalformedURLException | URISyntaxException ignored) {
            }
            httpPost = new HttpPost(url);
        }
        initHttpRequestBase(httpPost);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
        HttpEntity entity = null;
        try {
            entity = new StringEntity(requestData);
        } catch (UnsupportedEncodingException ignored) {
        }
        httpPost.setEntity(entity);

        return httpPost;
    }

    private HttpRequestBase initHttpRequestBase(HttpRequestBase httpRequestBase) {
        httpRequestBase.setHeader(HttpHeaders.USER_AGENT, userAgent);
        httpRequestBase.setHeader(HttpHeaders.ACCEPT_CHARSET, "UTF-8");
        httpRequestBase.setHeader(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

        return httpRequestBase;
    }

    private HttpClientBuilder createHandshakeFailureAllHttpClientBuilder() {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();

        try {
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial((chain, authType) -> true).build();

            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                    sslContext,
                    ArrayUtils.addAll(NOT_SECURE_PROTOCOLS, SECURE_PROTOCOLS), null,
                    NoopHostnameVerifier.INSTANCE
            );
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("https", new SSLConnectionSocketFactory(
                            sslContext,
                            SECURE_PROTOCOLS, // important
                            null,
                            NoopHostnameVerifier.INSTANCE))
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .build();

            HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
            httpClientBuilder.setSSLSocketFactory(sslConnectionSocketFactory).setConnectionManager(ccm);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        return httpClientBuilder;
    }
}