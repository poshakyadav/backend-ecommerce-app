package com.ecommerce.bkd.dtos.shiprocket;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ShiprocketExchangeOrderRequest {
    @JsonProperty("order_items")
    private List<OrderItem> orderItems;

    @JsonProperty("buyer_pickup_first_name")
    private String buyerPickupFirstName;

    @JsonProperty("buyer_pickup_last_name")
    private String buyerPickupLastName;

    @JsonProperty("buyer_pickup_email")
    private String buyerPickupEmail;

    @JsonProperty("buyer_pickup_address")
    private String buyerPickupAddress;

    @JsonProperty("buyer_pickup_address_2")
    private String buyerPickupAddress2;

    @JsonProperty("buyer_pickup_city")
    private String buyerPickupCity;

    @JsonProperty("buyer_pickup_state")
    private String buyerPickupState;

    @JsonProperty("buyer_pickup_country")
    private String buyerPickupCountry;

    @JsonProperty("buyer_pickup_phone")
    private String buyerPickupPhone;

    @JsonProperty("buyer_pickup_pincode")
    private String buyerPickupPincode;

    @JsonProperty("buyer_shipping_first_name")
    private String buyerShippingFirstName;

    @JsonProperty("buyer_shipping_last_name")
    private String buyerShippingLastName;

    @JsonProperty("buyer_shipping_email")
    private String buyerShippingEmail;

    @JsonProperty("buyer_shipping_address")
    private String buyerShippingAddress;

    @JsonProperty("buyer_shipping_address_2")
    private String buyerShippingAddress2;

    @JsonProperty("buyer_shipping_city")
    private String buyerShippingCity;

    @JsonProperty("buyer_shipping_state")
    private String buyerShippingState;

    @JsonProperty("buyer_shipping_country")
    private String buyerShippingCountry;

    @JsonProperty("buyer_shipping_phone")
    private String buyerShippingPhone;

    @JsonProperty("buyer_shipping_pincode")
    private String buyerShippingPincode;

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

    @JsonProperty("existing_order_id")
    private String existingOrderId;

    @JsonProperty("return_reason")
    private String returnReason;

    @JsonProperty("sub_total")
    private String subTotal;

    @JsonProperty("shipping_charges")
    private String shippingCharges;

    @JsonProperty("giftwrap_charges")
    private String giftwrapCharges;

    @JsonProperty("total_discount")
    private String totalDiscount;

    @JsonProperty("transaction_charges")
    private String transactionCharges;

    @JsonProperty("exchange_length")
    private String exchangeLength;

    @JsonProperty("exchange_breadth")
    private String exchangeBreadth;

    @JsonProperty("exchange_height")
    private String exchangeHeight;

    @JsonProperty("exchange_weight")
    private String exchangeWeight;

    @JsonProperty("return_length")
    private String returnLength;

    @JsonProperty("return_breadth")
    private String returnBreadth;

    @JsonProperty("return_height")
    private String returnHeight;

    @JsonProperty("return_weight")
    private String returnWeight;

    @JsonProperty("qc_check")
    private String qcCheck;

    @Data
    public static class OrderItem {
        private String name;
        @JsonProperty("selling_price")
        private String sellingPrice;
        private String units;
        private String hsn;
        private String sku;
        private String tax;
        private String discount;
        private String brand;
        private String color;
        @JsonProperty("exchange_item_id")
        private String exchangeItemId;
        @JsonProperty("exchange_item_name")
        private String exchangeItemName;
        @JsonProperty("exchange_item_sku")
        private String exchangeItemSku;
        @JsonProperty("qc_enable")
        private Boolean qcEnable;
        @JsonProperty("qc_product_name")
        private String qcProductName;
        @JsonProperty("qc_product_image")
        private String qcProductImage;
        @JsonProperty("qc_brand")
        private String qcBrand;
        @JsonProperty("qc_color")
        private String qcColor;
        @JsonProperty("qc_size")
        private String qcSize;
        private String accessories;
        @JsonProperty("qc_used_check")
        private String qcUsedCheck;
        @JsonProperty("qc_sealtag_check")
        private String qcSealtagCheck;
        @JsonProperty("qc_brand_box")
        private String qcBrandBox;
        @JsonProperty("qc_check_damaged_product")
        private String qcCheckDamagedProduct;
    }
}