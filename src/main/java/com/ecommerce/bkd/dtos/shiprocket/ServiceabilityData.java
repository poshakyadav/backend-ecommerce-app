package com.ecommerce.bkd.dtos.shiprocket;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceabilityData {
    @JsonProperty("available_courier_companies")
    private List<CourierServiceability> availableCourierCompanies;

    // (You can ignore all other fields under "data" if you do not need them.)
}