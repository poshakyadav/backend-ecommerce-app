package com.ecommerce.bkd.dtos;

import lombok.Data;

@Data
public class AddressDTO {
    private Long id;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String country;
    private String zipCode;
}
