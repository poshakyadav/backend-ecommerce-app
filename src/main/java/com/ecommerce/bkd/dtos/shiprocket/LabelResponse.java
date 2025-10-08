package com.ecommerce.bkd.dtos.shiprocket;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class LabelResponse {
    @JsonProperty("label_created")
    private int labelCreated;

    @JsonProperty("label_url")
    private String labelUrl;

    private String response;

    @JsonProperty("not_created")
    private List<Long> notCreated;
}
