package com.ecommerce.bkd.dtos.userAddress;

import com.ecommerce.bkd.models.enums.AddressDestinationType;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserAddressDTO {
    private Long id;
    private Long userId;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String country;
    private String pincode;
    private AddressDestinationType type;
    private boolean isServiceable;
    private String serviceabilityMessage;

    @JsonProperty("isPrimary")
    private boolean isPrimary;
}
