package com.ecommerce.bkd.dtos.shiprocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Maps the nested “pickup_generated_date” object:
 * {
 *   "date": "2021-12-10 12:39:54.034695",
 *   "timezone_type": 3,
 *   "timezone": "Asia/Kolkata"
 * }
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneratedDateWrapper {
    private String date;

    @JsonProperty("timezone_type")
    private Integer timezoneType;

    private String timezone;
}