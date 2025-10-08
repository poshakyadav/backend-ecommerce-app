package com.ecommerce.bkd.dtos.shiprocket;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Pagination {
    private int total;
    private int count;
    @JsonProperty("per_page")
    private int perPage;
    @JsonProperty("current_page")
    private int currentPage;
    @JsonProperty("total_pages")
    private int totalPages;
    private Map<String, String> links;
}