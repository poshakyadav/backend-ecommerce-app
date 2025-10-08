package com.ecommerce.bkd.dtos.products;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Data;

@Data
public class ProductSearchCriteria {
    private String q; // Search query
    private List<String> categories; // Category names
    private BigDecimal minPrice; // Minimum price
    private BigDecimal maxPrice; // Maximum price
    private Double minRating; // Minimum average rating
    private String sortBy = "relevance"; // Sorting: "relevance", "price-asc", "price-desc", "newest"
    private int page = 0; // Page number (0-indexed)
    private int size = 20; // Items per page
    private String tab = "all";

    // Constructor to handle comma-separated strings
    public ProductSearchCriteria(Map<String, String> params) {
        this.q = params.get("q");
        // this.categories = params.get("categories") != null ? Arrays.asList(params.get("categories").split(",")) : new ArrayList<>();
        this.categories = params.get("categories") != null 
            ? Arrays.stream(params.get("categories").split(","))
                .map(cat -> URLDecoder.decode(cat, StandardCharsets.UTF_8))
                .collect(Collectors.toList()) 
            : new ArrayList<>();
        this.minPrice = params.get("minPrice") != null ? new BigDecimal(params.get("minPrice")) : null;
        this.maxPrice = params.get("maxPrice") != null ? new BigDecimal(params.get("maxPrice")) : null;
        this.minRating = params.get("minRating") != null ? Double.valueOf(params.get("minRating")) : null;
        this.sortBy = params.getOrDefault("sortBy", "relevance");
        this.page = params.get("page") != null ? Integer.parseInt(params.get("page")) : 0;
        this.size = params.get("size") != null ? Integer.parseInt(params.get("size")) : 20;
        this.tab = params.getOrDefault("tab", "all");
    }
}
