package com.ecommerce.bkd.dtos.shiprocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShippedBy {

    @JsonProperty("shipper_company_name")
    private String shipperCompanyName;

    @JsonProperty("shipper_address_1")
    private String shipperAddress1;

    @JsonProperty("shipper_address_2")
    private String shipperAddress2;

    @JsonProperty("shipper_city")
    private String shipperCity;

    @JsonProperty("shipper_state")
    private String shipperState;

    @JsonProperty("shipper_country")
    private String shipperCountry;

    @JsonProperty("shipper_postcode")
    private String shipperPostcode;

    @JsonProperty("shipper_first_mile_activated")
    private Integer shipperFirstMileActivated;

    @JsonProperty("shipper_phone")
    private String shipperPhone;

    @JsonProperty("lat")
    private String latitude;

    @JsonProperty("long")
    private String longitude;

    @JsonProperty("shipper_email")
    private String shipperEmail;

    @JsonProperty("rto_company_name")
    private String rtoCompanyName;

    @JsonProperty("rto_address_1")
    private String rtoAddress1;

    @JsonProperty("rto_address_2")
    private String rtoAddress2;

    @JsonProperty("rto_city")
    private String rtoCity;

    @JsonProperty("rto_state")
    private String rtoState;

    @JsonProperty("rto_country")
    private String rtoCountry;

    @JsonProperty("rto_postcode")
    private String rtoPostcode;

    @JsonProperty("rto_phone")
    private String rtoPhone;

    @JsonProperty("rto_email")
    private String rtoEmail;
}