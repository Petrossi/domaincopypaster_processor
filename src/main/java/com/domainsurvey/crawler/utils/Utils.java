package com.domainsurvey.crawler.utils;

import com.domainsurvey.crawler.service.fetcher.model.HttpConfig;
import org.apache.http.HttpStatus;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.Adler32;
import java.util.zip.CRC32;

public class Utils {

    public static boolean isRedirected(int statusCode) {
        return statusCode == HttpStatus.SC_MOVED_PERMANENTLY ||
                statusCode == HttpStatus.SC_MOVED_TEMPORARILY ||
                statusCode == HttpStatus.SC_MULTIPLE_CHOICES ||
                statusCode == HttpStatus.SC_SEE_OTHER ||
                statusCode == HttpStatus.SC_TEMPORARY_REDIRECT ||
                statusCode == 308
                ;
    }

    public static boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    public static boolean isValidContentType(String type, String contentType) {
        if (type.equals(HttpConfig.POST_REQUEST)) {
            return contentType.contains("text/plain") || contentType.equalsIgnoreCase("application/json");
        }
        return isValidHttpGetContentType(contentType);
    }

    public static boolean isValidHttpGetContentType(String contentType) {
        return contentType.contains("html") || contentType.equalsIgnoreCase("text/plain");
    }

    public static long getCRC32(String value) {
        if (value == null) {
            return 0;
        }

        value = UUID.nameUUIDFromBytes(value.getBytes()).toString();

        Adler32 adler = new Adler32();
        CRC32 crc = new CRC32();

        int rightBits = 0;
        for (int i = 0; i < value.length(); i++) {
            rightBits = 31 * rightBits + value.charAt(i); //yep, that's what String.hashCode does
        }

        adler.reset();

        byte[] bytes = value.getBytes();
        adler.update(bytes, 0, bytes.length);

        long checksum = adler.getValue() * (1 + (rightBits * 31L));

        if (value.length() < 10) { //small strings tend to collide a little more
            // using only the hash + adler above, so we add CRC here.
            crc.reset();
            crc.update(bytes, 0, bytes.length);
            checksum = crc.getValue() * (1 + (checksum * 31));
        }

        return checksum;
    }

    public static String getDomainHostFromURL(String url) {
        if (!url.startsWith("http:") && !url.startsWith("https:")) {
            url = "http://" + url;
        }

        try {
            URL url1 = new URL(url);
            return url1.getHost();
        } catch (MalformedURLException e) {
            return url;
        }
    }

    public static String convertFomDBValidValueURL(String url) {
        int maxIteration = 15;
        int iterationCount = 0;
        if (url.contains("%")) {
            while (url.contains("%") && maxIteration > iterationCount) {
                try {
                    url = URLDecoder.decode(url, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    return url;
                }
                iterationCount++;
            }
        }

        return url;
    }

    public static String limit(String origin, int limit) {
        if (origin == null) {
            return null;
        }
        if (origin.length() > limit) {
            origin = origin.substring(0, limit - 1);
        }
        return origin;
    }

    public static void sleepSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException ignored) {
        }
    }

    public static String getValidDomainUrl(String urlStr) throws MalformedURLException {
        URL url = new URL(urlStr);

        return String.format("%s://%s", url.getProtocol(), url.getHost());
    }

    public static boolean isApplicationLink(String url) {
        if (url.startsWith("http") || url.startsWith("#") || url.startsWith("/") || url.startsWith(":/") || !url.contains(":")) {
            return false;
        }

        return url.contains(":");
    }

    public static Timestamp getCurrentTimestamp() {
        return Timestamp.from(Instant.now());
    }

}