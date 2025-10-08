package com.ecommerce.bkd.dtos.shiprocket;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiprocketReturnOrderResponse {
    @JsonProperty("order_id")
    private Long orderId;
    @JsonProperty("shipment_id")
    private Long shipmentId;
    private String status;
    @JsonProperty("status_code")
    private Integer statusCode;
    @JsonProperty("company_name")
    private String companyName;
}
