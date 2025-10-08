package com.ecommerce.bkd.dtos.shiprocket;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class NdrShipment {
    private Long id;
    @JsonProperty("shipment_id")
    private Long shipmentId;

    @JsonProperty("customer_name")
    private String customerName;
    @JsonProperty("customer_email")
    private String customerEmail;
    @JsonProperty("customer_phone")
    private String customerPhone;
    @JsonProperty("customer_address")
    private String customerAddress;
    @JsonProperty("customer_address_2")
    private String customerAddress2;
    @JsonProperty("customer_city")
    private String customerCity;
    @JsonProperty("customer_state")
    private String customerState;
    @JsonProperty("customer_pincode")
    private String customerPincode;
    @JsonProperty("payment_status")
    private String paymentStatus;
    private String status;
    @JsonProperty("status_code")
    private Integer statusCode;
    @JsonProperty("payment_method")
    private String paymentMethod;
    @JsonProperty("created_at")
    private String createdAt;
    private String reason;
    private Integer attempts;
    @JsonProperty("ndr_raised_at")
    private String ndrRaisedAt;
    private String courier;
    @JsonProperty("awb_code")
    private String awbCode;
    @JsonProperty("escalation_status")
    private String escalationStatus;
    @JsonProperty("product_name")
    private String productName;
    @JsonProperty("product_price")
    private String productPrice;
    @JsonProperty("shipment_channel_id")
    private Long shipmentChannelId;
    private List<NdrHistory> history;
    @JsonProperty("delivered_date")
    private String deliveredDate;
}
