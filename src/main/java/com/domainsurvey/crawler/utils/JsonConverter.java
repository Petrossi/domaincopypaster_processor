package com.domainsurvey.crawler.utils;

import lombok.SneakyThrows;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * Useful class to convert to and from Json In this example we use Google gson
 */
public class JsonConverter {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * This method deserializes the specified Json into an object of the
     * specified class.
     */
    @SneakyThrows
    public static <T> T convertFromJson(String toConvert, Class<T> clazz) {

        return mapper.readValue(toConvert, clazz);
    }

    @SneakyThrows
    public static <T> List<T> convertListFromJson(String str, Class<? extends Collection> type, Class<T> elementType) {
        return mapper.readValue(str, mapper.getTypeFactory().constructCollectionType(type, elementType));
    }

    /**
     * This method serializes the specified object into its equivalent Json
     * representation.
     */
    @SneakyThrows
    public static String convertToJson(Object toConvert) {

        return mapper.writeValueAsString(toConvert);
    }
}