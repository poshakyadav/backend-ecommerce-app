package com.ecommerce.bkd.dtos.shiprocket;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ManifestResponse {
    private int status; // always 1 when successful
    @JsonProperty("manifest_url")
    private String manifestUrl;
}