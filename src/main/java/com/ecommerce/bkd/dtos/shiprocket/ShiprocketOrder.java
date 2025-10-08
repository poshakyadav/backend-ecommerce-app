package com.ecommerce.bkd.dtos.shiprocket;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
public class ShiprocketOrder {

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("order_date")
    private String orderDate;

    @JsonProperty("pickup_location")
    private String pickupLocation;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("billing_customer_name")
    private String billingCustomerName;

    @JsonProperty("billing_last_name")
    private String billingLastName;

    @JsonProperty("billing_address")
    private String billingAddress;

    @JsonProperty("billing_address_2")
    private String billingAddress2;

    @JsonProperty("billing_city")
    private String billingCity;

    @JsonProperty("billing_pincode")
    private Integer billingPincode;

    @JsonProperty("billing_state")
    private String billingState;

    @JsonProperty("billing_country")
    private String billingCountry;

    @JsonProperty("billing_email")
    private String billingEmail;

    @JsonProperty("billing_phone")
    private String billingPhone;

    @JsonProperty("billing_alternate_phone")
    private String billingAlternatePhone;

    @JsonProperty("shipping_is_billing")
    private boolean shippingIsBilling;

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

    @JsonProperty("shipping_pincode")
    private String shippingPincode;

    @JsonProperty("shipping_state")
    private String shippingState;

    @JsonProperty("shipping_country")
    private String shippingCountry;

    @JsonProperty("shipping_email")
    private String shippingEmail;

    @JsonProperty("shipping_phone")
    private String shippingPhone;

    @JsonProperty("payment_method")
    private String paymentMethod;

    @JsonProperty("order_items")
    private List<ShiprocketOrderItem> orderItems;

    @JsonProperty("shipping_charges")
    private BigDecimal shippingCharges;

    @JsonProperty("giftwrap_charges")
    private BigDecimal giftwrapCharges;

    @JsonProperty("transaction_charges")
    private BigDecimal transactionCharges;

    @JsonProperty("total_discount")
    private BigDecimal totalDiscount;

    @JsonProperty("sub_total")
    private BigDecimal subTotal;

    @JsonProperty("length")
    private BigDecimal length;

    @JsonProperty("breadth")
    private BigDecimal breadth;

    @JsonProperty("height")
    private BigDecimal height;

    @JsonProperty("weight")
    private BigDecimal weight;

    // Return Order Request fields
    @JsonProperty("tracking_number")
    private String trackingNumber;

    @JsonProperty("payment_status")
    private String paymentStatus;

    @JsonProperty("shipment_status")
    private String shipmentStatus;

    @JsonProperty("awb")
    private String awb;

    @JsonProperty("courier_name")
    private String courierName;

    @JsonProperty("channel_id")
    private Long channelId;
}
