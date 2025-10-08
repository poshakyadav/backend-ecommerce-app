package com.ecommerce.bkd.dtos.shiprocket;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class ShiprocketOrderDetailResponse {

    private OrderDetail data;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    @NoArgsConstructor
    public static class OrderDetail {
        // basic identifiers
        private Long id;
        @JsonProperty("channel_id")
        private Long channelId;
        @JsonProperty("channel_name")
        private String channelName;
        @JsonProperty("base_channel_code")
        private String baseChannelCode;
        @JsonProperty("channel_order_id")
        private String channelOrderId;

        // flags
        @JsonProperty("is_international")
        private Integer international;
        @JsonProperty("is_document")
        private Integer document;
        @JsonProperty("is_return")
        private Integer returnFlag;
        @JsonProperty("is_incomplete")
        private Integer incomplete;

        // customer info
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
        @JsonProperty("customer_country")
        private String customerCountry;

        // pickup info
        @JsonProperty("pickup_code")
        private String pickupCode;
        @JsonProperty("pickup_location")
        private String pickupLocation;
        @JsonProperty("pickup_location_id")
        private String pickupLocationId;
        @JsonProperty("pickup_id")
        private String pickupId;

        // financials & status
        private String currency;
        @JsonProperty("country_code")
        private Integer countryCode;
        private BigDecimal total;
        @JsonProperty("net_total")
        private String netTotal;
        private BigDecimal discount;
        @JsonProperty("other_charges")
        private String otherCharges;
        @JsonProperty("other_discounts")
        private String otherDiscounts;
        @JsonProperty("giftwrap_charges")
        private String giftwrapCharges;
        private BigDecimal tax;
        @JsonProperty("cod")
        private Integer cod;
        @JsonProperty("payment_method")
        private String paymentMethod;
        private String status;
        @JsonProperty("status_code")
        private Integer statusCode;
        @JsonProperty("sub_status")
        private String subStatus;
        @JsonProperty("master_status")
        private String masterStatus;

        // timing
        @JsonProperty("channel_created_at")
        private String channelCreatedAt;
        @JsonProperty("created_at")
        private String createdAt;
        @JsonProperty("order_date")
        private String orderDate;
        @JsonProperty("updated_at")
        private String updatedAt;

        /** Fulfillment status only for SRF orders */
        @JsonProperty("fulfillment_status")
        private String fulfillmentStatus;

        // SLA / shipping
        private String sla;
        @JsonProperty("ship_type")
        private String shipType;
        @JsonProperty("courier_mode")
        private String courierMode;
        @JsonProperty("delivery_code")
        private String deliveryCode;
        private String expedited;

        // collections
        private List<Product> products;
        private Shipment shipments;
        @JsonProperty("awb_data")
        private AwbData awbData;
        @JsonProperty("order_insurance")
        private OrderInsurance orderInsurance;
        @JsonProperty("return_pickup_data")
        private ReturnPickup returnPickupData;

        // billing snapshots
        @JsonProperty("billing_name")
        private String billingName;
        @JsonProperty("billing_email")
        private String billingEmail;
        @JsonProperty("billing_phone")
        private String billingPhone;
        @JsonProperty("billing_address")
        private String billingAddress;
        @JsonProperty("billing_address_2")
        private String billingAddress2;
        @JsonProperty("billing_city")
        private String billingCity;
        @JsonProperty("billing_state_name")
        private String billingStateName;
        @JsonProperty("billing_country_name")
        private String billingCountryName;
        @JsonProperty("billing_pincode")
        private String billingPincode;

        // “others” catch-all
        // If you want to capture any other ⚓ ad-hoc fields:
        @JsonAnySetter
        private Map<String, Object> others;

        // QC, coupon, refund, extra info, etc.
        private List<ProductQc> productQc;
        @JsonProperty("refund_detail")
        private RefundDetail refundDetail;
        private ExtraInfo extraInfo;

        @Data
        @NoArgsConstructor
        public static class Product {
            private Long id;
            @JsonProperty("product_id")
            private Long productId;
            private String name;
            private String sku;
            private String description;
            private BigDecimal price;
            private Integer quantity;
            private BigDecimal netTotal;
            private Integer returnableQuantity;
            private Integer tax;
            private Integer status;
            private BigDecimal discount;
            private String dimensions;
            private Integer weight;
        }

        @Data
        @NoArgsConstructor
        public static class Shipment {
            private Long id;
            @JsonProperty("order_id")
            private Long orderId;
            private String status;
            private String awb;
            private BigDecimal weight;
            private BigDecimal volumetricWeight;
            private String dimensions;
            @JsonProperty("courier")
            private String courierName;
            @JsonProperty("courier_id")
            private String courierId;
            @JsonProperty("created_at")
            private String createdAt;
            @JsonProperty("updated_at")
            private String updatedAt;
            @JsonProperty("delivered_date")
            private String deliveredDate;
            @JsonProperty("shipped_date")
            private String shippedDate;
        }

        @Data
        @NoArgsConstructor
        public static class AwbData {
            private String awb;
            @JsonProperty("applied_weight")
            private String appliedWeight;
            @JsonProperty("charged_weight")
            private String chargedWeight;
            private Charges charges;

            @Data
            @NoArgsConstructor
            public static class Charges {
                private String zone;
                @JsonProperty("cod_charges")
                private String codCharges;
                @JsonProperty("freight_charges")
                private String freightCharges;
                // etc...
            }
        }

        @Data
        @NoArgsConstructor
        public static class OrderInsurance {
            @JsonProperty("insurance_status")
            private String insuranceStatus;
            @JsonProperty("policy_no")
            private String policyNo;
            @JsonProperty("claim_enable")
            private Boolean claimEnable;
        }

        @Data
        @NoArgsConstructor
        public static class ReturnPickup {
            private Long id;
            private String name;
            private String email;
            private String address;
            @JsonProperty("address_2")
            private String address2;
            private String city;
            private String state;
            private String country;
            @JsonProperty("pin_code")
            private String pinCode;
            private String phone;
        }

        @Data
        @NoArgsConstructor
        public static class ProductQc {
            @JsonProperty("product_id")
            private Long productId;
            @JsonProperty("qc_values")
            private Map<String, QcValue> qcValues;

            @Data
            @NoArgsConstructor
            public static class QcValue {
                private String value;
                private String name;
            }
        }

        @Data
        @NoArgsConstructor
        public static class RefundDetail {
            @JsonProperty("refund_mode")
            private String refundMode;
            @JsonProperty("account_holder_name")
            private String accountHolderName;
            @JsonProperty("account_number")
            private String accountNumber;
            @JsonProperty("bank_ifsc")
            private String bankIfsc;
            @JsonProperty("bank_name")
            private String bankName;
        }

        @Data
        @NoArgsConstructor
        public static class ExtraInfo {
            @JsonProperty("qc_check")
            private Integer qcCheck;
            @JsonProperty("qc_params")
            private String qcParams;
            @JsonProperty("order_type")
            private Integer orderType;
            @JsonProperty("amazon_dg_status")
            private Boolean amazonDgStatus;
            @JsonProperty("insurace_opted_at_order_creation")
            private Boolean insuranceOpted;
            // etc...
        }
    }
}
