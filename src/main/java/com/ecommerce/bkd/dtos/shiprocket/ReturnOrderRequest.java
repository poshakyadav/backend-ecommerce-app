package com.ecommerce.bkd.dtos.shiprocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload for creating a Return Order
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnOrderRequest {

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("order_date")
    private String orderDate;

    @JsonProperty("channel_id")
    private String channelId;

    @JsonProperty("pickup_customer_name")
    private String pickupCustomerName;

    @JsonProperty("pickup_last_name")
    private String pickupLastName;

    @JsonProperty("company_name")
    private String companyName;

    private String returnReason;//...
    private String shippingDate;//...

    @JsonProperty("pickup_address")
    private String pickupAddress;

    @JsonProperty("pickup_address_2")
    private String pickupAddress2;

    @JsonProperty("pickup_city")
    private String pickupCity;

    @JsonProperty("pickup_state")
    private String pickupState;

    @JsonProperty("pickup_country")
    private String pickupCountry;

    @JsonProperty("pickup_pincode")
    private Integer pickupPincode;

    @JsonProperty("pickup_email")
    private String pickupEmail;

    @JsonProperty("pickup_phone")
    private String pickupPhone;

    @JsonProperty("pickup_isd_code")
    private String pickupIsdCode;

    @JsonProperty("shipping_customer_name")
    private String shippingCustomerName;

    @JsonProperty("shipping_last_name")
    private String shippingLastName;

    @JsonProperty("shipping_address")
    private String shippingAddress;

    @JsonProperty("shipping_address_2")
    private String shippingAddress2;

    @JsonProperty("shipping_city")
    private String shippingCity;

    @JsonProperty("shipping_country")
    private String shippingCountry;
    
    @JsonProperty("shipping_pincode")
    private Integer shippingPincode;

    @JsonProperty("shipping_state")
    private String shippingState;

    @JsonProperty("shipping_email")
    private String shippingEmail;

    @JsonProperty("shipping_isd_code")
    private String shippingIsdCode;

    @JsonProperty("shipping_phone")
    private Long shippingPhone;

    @JsonProperty("order_items")
    private List<ReturnOrderItem> orderItems;

    @JsonProperty("payment_method")
    private String paymentMethod;

    @JsonProperty("total_discount")
    private String totalDiscount;

    @JsonProperty("sub_total")
    private Long subTotal;

    private Float length;
    private Float breadth;
    private Float height;
    private Float weight;
}
