package com.ecommerce.bkd.dtos.shiprocket;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiprocketReturnOrdersResponse {
    private List<ReturnOrderSummary> data;
    private Meta meta;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReturnOrderSummary {
        private Long id;
        @JsonProperty("channel_id")
        private Integer channelId;
        @JsonProperty("channel_name")
        private String channelName;
        @JsonProperty("base_channel_code")
        private String baseChannelCode;
        @JsonProperty("channel_order_id")
        private String channelOrderId;
        @JsonProperty("customer_name")
        private String customerName;
        @JsonProperty("customer_email")
        private String customerEmail;
        @JsonProperty("customer_phone")
        private String customerPhone;
        @JsonProperty("customer_pincode")
        private String customerPincode;
        @JsonProperty("pickup_location")
        private String pickupLocation;
        private String status;
        @JsonProperty("status_code")
        private Integer statusCode;
        @JsonProperty("shipment_id")
        private Long shipmentId;
        private List<Product> products;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Product {
        private Long id;
        private String name;
        @JsonProperty("channel_sku")
        private String channelSku;
        private Integer quantity;
        private String sku;
        private String hsn;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        private Pagination pagination;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pagination {
        private Integer total;
        private Integer count;
        @JsonProperty("per_page")
        private Integer perPage;
        @JsonProperty("current_page")
        private Integer currentPage;
        @JsonProperty("total_pages")
        private Integer totalPages;
        private Links links;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Links {
        private String next;
    }
}
