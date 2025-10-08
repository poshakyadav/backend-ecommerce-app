package com.ecommerce.bkd.dtos.shiprocket;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PrintManifestResponse {

    /**
     * URL to the generated manifest PDF
     */
    @JsonProperty("manifest_url")
    private String manifestUrl;

}
