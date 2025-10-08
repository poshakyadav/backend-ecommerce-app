package com.ecommerce.bkd.dtos.shiprocket;

import java.util.Optional;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NdrFilter {
    private int page;
    private int perPage;
    private String from;
    private String to;
    private String search;

    public Optional<String> getFromDate() {
        return Optional.ofNullable(from);
    }

    public Optional<String> getToDate() {
        return Optional.ofNullable(to);
    }

    public Optional<String> getSearchTerm() {
        return Optional.ofNullable(search);
    }
}
