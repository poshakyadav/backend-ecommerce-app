package com.ecommerce.bkd.dtos.shipping;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PickupLocationRequest {

    @JsonProperty("pickup_location")
    private String pickupLocation; // Home or Office

    @JsonProperty("name")
    private String name; // Shipper’s name

    @JsonProperty("address")
    private String address;

    @JsonProperty("address_2")
    private String address2; // optional

    @JsonProperty("city")
    private String city;

    @JsonProperty("state")
    private String state;

    @JsonProperty("country")
    private String country;

    @JsonProperty("pin_code")
    private String pincode;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("email")
    private String email;

    @JsonProperty("lat")
    private Double latitude; // optional

    @JsonProperty("long")
    private Double longitude; // optional

    @JsonProperty("address_type")
    private String addressType; // optional

    @JsonProperty("vendor_name")
    private String vendorName; // optional

    @JsonProperty("gstin")
    private String gstin; // optional
}
