package com.domainsurvey.crawler.dao.util;

public class DomainIdGenerator {

    public static final byte ID_LENGTH = 10;

    public static String generateNewId() {
        String AlphaNumericString = "abcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(ID_LENGTH);

        for (int i = 0; i < ID_LENGTH; i++) {
            int index = (int) (AlphaNumericString.length() * Math.random());

            sb.append(AlphaNumericString.charAt(index));
        }
        return sb.toString();
    }
}