package com.ecommerce.bkd.models;

import com.ecommerce.bkd.models.enums.AddressDestinationType;
import com.ecommerce.bkd.models.enums.AddressType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String address1;

    private String address2;

    @NotBlank
    private String city;

    private String state;

    @NotBlank
    private String country;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid pincode format")
    private String pincode; // Changed from zipCode

    // This field is now useless, so do not touch this field
    @Enumerated(EnumType.STRING)
    private AddressType type; // SHIPPING, BILLING, BOTH

    private AddressDestinationType destinationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    private boolean isServiceable;
    private String serviceabilityMessage;
    private boolean isPrimary;

}
