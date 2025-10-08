package com.ecommerce.bkd.dtos.shiprocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignedDateTime {

    @JsonProperty("date")
    private String date;

    @JsonProperty("timezone_type")
    private Integer timezoneType;

    @JsonProperty("timezone")
    private String timezone;
}