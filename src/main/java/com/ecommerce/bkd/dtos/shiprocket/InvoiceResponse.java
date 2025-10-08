package com.ecommerce.bkd.dtos.shiprocket;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class InvoiceResponse {
    @JsonProperty("download_url")
    private String downloadUrl;
}
