package com.ecommerce.bkd.dtos.shiprocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenerateAwbResponse {

    /**
     * Corresponds to the inner "data" object in the JSON:
     * {
     *   "response": {
     *     "data": { ... }
     *   }
     * }
     */
    @JsonProperty("data")
    private AwbData data;
}