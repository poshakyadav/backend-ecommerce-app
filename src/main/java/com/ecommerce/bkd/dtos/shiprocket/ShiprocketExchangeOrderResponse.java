package com.ecommerce.bkd.dtos.shiprocket;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiprocketExchangeOrderResponse {
    private boolean success;
    private ExchangeData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExchangeData {

        @JsonProperty("forward_orders")
        private OrderDetails forwardOrders;
        @JsonProperty("return_orders")
        private OrderDetails returnOrders;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDetails {
        @JsonProperty("order_id")
        private Long orderId;
        @JsonProperty("channel_order_id")
        private String channelOrderId;
        @JsonProperty("shipment_id")
        private Long shipmentId;
        private String status;
        @JsonProperty("status_code")
        private Integer statusCode;
        @JsonProperty("awb_code")
        private String awbCode;
        @JsonProperty("courier_company_id")
        private String courierCompanyId;
        @JsonProperty("courier_name")
        private String courierName;
    }
}
