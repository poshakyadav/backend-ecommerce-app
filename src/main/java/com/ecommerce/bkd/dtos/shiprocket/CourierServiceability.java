package com.ecommerce.bkd.dtos.shiprocket;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourierServiceability {

    @JsonProperty("courier_company_id")
    private Integer courierCompanyId;

    @JsonProperty("courier_name")
    private String courierName;

    /** Total rate (freight_charge + any other applicable) */
    @JsonProperty("rate")
    private BigDecimal rate;

    /** Estimated delivery days (string in response, but map to Integer) */
    @JsonProperty("estimated_delivery_days")
    private Integer estimatedDeliveryDays;

    /** 1 = COD allowed, 0 = Prepaid-only */
    @JsonProperty("cod")
    private Integer cod;

    /** Flat COD charge */
    @JsonProperty("cod_charges")
    private BigDecimal codCharges;

    /** Percentage multiplier on COD amount (e.g. 0.01 for 1%) */
    @JsonProperty("cod_multiplier")
    private BigDecimal codMultiplier;

    /** Minimum weight slab this rate applies to */
    @JsonProperty("min_weight")
    private BigDecimal minWeight;

    /** Maximum surface weight allowed */
    @JsonProperty("surface_max_weight")
    private BigDecimal surfaceMaxWeight;

    /** Maximum air weight allowed (if applicable) */
    @JsonProperty("air_max_weight")
    private BigDecimal airMaxWeight;

    /** Volumetric max weight, if any */
    @JsonProperty("volumetric_max_weight")
    private BigDecimal volumetricMaxWeight;

    /** RTO (return-to-origin) charges, if any */
    @JsonProperty("rto_charges")
    private BigDecimal rtoCharges;

    /** Postcode this response applies to (usually same as pickup_postcode) */
    @JsonProperty("postcode")
    private String postcode;

    /** City name for the pickup/delivery */
    @JsonProperty("city")
    private String city;

    /** State for the pickup/delivery */
    @JsonProperty("state")
    private String state;

    /** Any other “other_charges” */
    @JsonProperty("other_charges")
    private BigDecimal otherCharges;

    /** “charge_weight” field—used internally by Shiprocket */
    @JsonProperty("charge_weight")
    private BigDecimal chargeWeight;

    /** True if this courier uses a custom rate (1) or standard (0) */
    @JsonProperty("is_custom_rate")
    private Integer isCustomRate;

    /** Whether this is surface shipment (1) or air (0) */
    @JsonProperty("is_surface")
    private Boolean isSurface;

    /** Estimated transit days formatted as string, e.g. “4” */
    @JsonProperty("edd")
    private String edd;


}
