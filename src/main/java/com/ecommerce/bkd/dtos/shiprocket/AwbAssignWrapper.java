package com.ecommerce.bkd.dtos.shiprocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AwbAssignWrapper {

    @JsonProperty("awb_assign_status")
    private Integer awbAssignStatus;

    /**
     * The “response” object contains a single “data” field,
     * which holds all the AWB‐related details.
     */
    @JsonProperty("response")
    private GenerateAwbResponse response;
}