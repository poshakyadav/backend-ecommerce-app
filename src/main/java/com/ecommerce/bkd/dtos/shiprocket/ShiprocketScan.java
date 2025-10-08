package com.ecommerce.bkd.dtos.shiprocket;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A single scan/activity record inside a Shiprocket webhook.
 */
@Data
public class ShiprocketScan {

    @JsonProperty("date")
    private String date; // e.g. "2023-05-19 11:59:16"

    @JsonProperty("status")
    private String status; // e.g. "X-UCI"

    @JsonProperty("activity")
    private String activity; // e.g. "Manifested - Manifest uploaded"

    @JsonProperty("location")
    private String location; // e.g. "Chomu_SamodRd_D (Rajasthan)"

    @JsonProperty("sr-status")
    private String srStatus; // e.g. "5"

    @JsonProperty("sr-status-label")
    private String srStatusLabel; // e.g. "MANIFEST GENERATED"
}
