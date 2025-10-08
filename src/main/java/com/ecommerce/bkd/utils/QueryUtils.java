package com.ecommerce.bkd.utils;

import java.util.Map;
import java.util.stream.Collectors;

public class QueryUtils {
    public static String buildQueryParams(Map<String, Object> params) {
        return params.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
    }
}
