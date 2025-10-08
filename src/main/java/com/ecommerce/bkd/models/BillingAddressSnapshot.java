package com.ecommerce.bkd.models;

import jakarta.persistence.*;
import lombok.*;

import com.ecommerce.bkd.models.enums.AddressType;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BillingAddressSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String lastName;
    private String email;
    private String phone;

    private String address1;
    private String address2;
    private String city;
    private String state;
    private String country;
    private String pinCode;

    @Enumerated(EnumType.STRING)
    private AddressType type;
    private boolean isServiceable;
    private String serviceabilityMessage;
    private boolean isPrimary;

    @JsonBackReference
    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    // Constructor to create snapshot from UserAddress
    public BillingAddressSnapshot(UserAddress address) {
        this.name = address.getUser().getFirstName();
        this.lastName = address.getUser().getLastName();
        this.email = address.getUser().getEmail();
        this.phone = address.getUser().getPhone();
        this.address1 = address.getAddress1();
        this.address2 = address.getAddress2();
        this.city = address.getCity();
        this.state = address.getState();
        this.country = address.getCountry();
        this.pinCode = address.getPincode();
        this.type = address.getType();
        this.isServiceable = address.isServiceable();
        this.serviceabilityMessage = address.getServiceabilityMessage();
        this.isPrimary = address.isPrimary();
    }
}
