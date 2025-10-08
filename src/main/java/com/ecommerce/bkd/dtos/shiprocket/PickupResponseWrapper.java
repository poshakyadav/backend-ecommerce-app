package com.ecommerce.bkd.dtos.shiprocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Holds the “response” block under the top-level JSON.
 * {
 *   "pickup_scheduled_date": "2021-12-10 12:39:54",
 *   "pickup_token_number": "...",
 *   "status": 3,
 *   "others": "...",
 *   "pickup_generated_date": { … },
 *   "data": "Pickup is confirmed by Xpressbees …"
 * }
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PickupResponseWrapper {
    @JsonProperty("pickup_scheduled_date")
    private String pickupScheduledDate;

    @JsonProperty("pickup_token_number")
    private String pickupTokenNumber;

    private Integer status;

    private String others;

    @JsonProperty("pickup_generated_date")
    private GeneratedDateWrapper pickupGeneratedDate;

    private String data;
}
