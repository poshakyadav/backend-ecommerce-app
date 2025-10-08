package com.ecommerce.bkd.dtos.shiprocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Top-level wrapper for Shiprocket’s “generate pickup” response.
 * {
 *   "pickup_status": 1,
 *   "response": { ... }
 * }
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PickupAssignWrapper {
    @JsonProperty("pickup_status")
    private Integer pickupStatus;

    @JsonProperty("response")
    private PickupResponseWrapper response;
}