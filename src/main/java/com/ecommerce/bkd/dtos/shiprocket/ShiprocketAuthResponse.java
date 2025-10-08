package com.ecommerce.bkd.dtos.shiprocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ShiprocketAuthResponse {
    private String token;
    
    @JsonProperty("user_id")
    private Long userId;
    
    @JsonProperty("first_name")
    private String firstName;
    
    @JsonProperty("last_name")
    private String lastName;
    
    @JsonProperty("company_id")
    private Long companyId;
    
    private String email;
}
