package com.ecommerce.bkd.dtos.shiprocket;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeOrderRequest {

    @JsonProperty("order_items")
    private List<ExchangeOrderItem> orderItems;

    @JsonProperty("buyer_pickup_first_name")
    private String buyerPickupFirstName;
    // ... other buyer pickup & shipping fields omitted for brevity

    @JsonProperty("seller_pickup_location_id")
    private String sellerPickupLocationId;
    @JsonProperty("seller_shipping_location_id")
    private String sellerShippingLocationId;

    @JsonProperty("exchange_order_id")
    private String exchangeOrderId;
    @JsonProperty("return_order_id")
    private String returnOrderId;

    @JsonProperty("payment_method")
    private String paymentMethod;
    @JsonProperty("order_date")
    private String orderDate;
    @JsonProperty("channel_id")
    private String channelId;

    private String existingOrderId;
    private String returnReason;
    private BigDecimal subTotal;
    private BigDecimal shippingCharges;
    private BigDecimal giftwrapCharges;
    private BigDecimal totalDiscount;
    private BigDecimal transactionCharges;

    @JsonProperty("exchange_length")
    private BigDecimal exchangeLength;
    @JsonProperty("exchange_breadth")
    private BigDecimal exchangeBreadth;
    @JsonProperty("exchange_height")
    private BigDecimal exchangeHeight;
    @JsonProperty("exchange_weight")
    private BigDecimal exchangeWeight;

    @JsonProperty("return_length")
    private BigDecimal returnLength;
    @JsonProperty("return_breadth")
    private BigDecimal returnBreadth;
    @JsonProperty("return_height")
    private BigDecimal returnHeight;
    @JsonProperty("return_weight")
    private BigDecimal returnWeight;

    @JsonProperty("qc_check")
    private boolean qcCheck;
}
