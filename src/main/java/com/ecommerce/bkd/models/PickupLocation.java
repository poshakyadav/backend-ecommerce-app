package com.ecommerce.bkd.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class PickupLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pickupLocation;

    /**These two fields stored same data */
    private Long shiprocketPickupId;
    private Long srId; // this is shiprocket 'id' fields

    private Long companyId;
    private String pickupCode;
    private String name;
    private String email;
    private String phone;
    private String alternatePhone;
    private String address;
    private String address2;
    private String addressType;
    private String city;
    private String state;
    private String country;
    private String pincode;
    private String gstin;
    private Double latitude;
    private Double longitude;
    private Integer status;
    private Boolean phoneVerified;
    private Long rtoAddressId;
    private String associatedRtoAddress;// "associated_rto_address"
    private String extraInfo;
   
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String companyName;
    private String fullName;

    private Boolean isPrimaryLocation; // Indicates if this is the primary pickup location for the company
}
