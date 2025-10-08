package com.ecommerce.bkd.dtos.shiprocket;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CreateOrderResponse {

    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("shipment_id")
    private Long shipmentId;

    @JsonProperty("awb_code")
    private String awbCode;

    @JsonProperty("courier_company_id")
    private Integer courierCompanyId;

    @JsonProperty("courier_name")
    private String courierName;

    @JsonProperty("status")
    private String status;

    @JsonProperty("status_code")
    private Integer statusCode;

    @JsonProperty("onboarding_completed_now")
    private Integer onboardingCompletedNow;

}
