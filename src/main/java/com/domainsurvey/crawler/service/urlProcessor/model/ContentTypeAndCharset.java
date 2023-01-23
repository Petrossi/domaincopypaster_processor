package com.domainsurvey.crawler.service.urlProcessor.model;

import lombok.Getter;

@Getter
public class ContentTypeAndCharset {
    private String charsetType = "UTF-8";
    private String contentType;

    public ContentTypeAndCharset(String contentTypeAndCharset){
        parse(contentTypeAndCharset);
    }

    private void parse(String contentTypeAndCharset){
        final String charset = "charset=";

        try{
            for (String param : contentTypeAndCharset.toLowerCase().replace(" ", "").split(";")) {
                if (param.toLowerCase().startsWith(charset) && !param.substring(charset.length()).equalsIgnoreCase("none")) {
                    charsetType = param.substring(charset.length()).replace("\"", "");
                    break;
                }
            }
        }catch (Exception ignored){}

        contentType = contentTypeAndCharset.toLowerCase().replace(charset + charsetType, "").replace(";", "").trim();
    }
}