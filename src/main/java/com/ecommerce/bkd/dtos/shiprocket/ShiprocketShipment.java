package com.ecommerce.bkd.dtos.shiprocket;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class ShiprocketShipment {
    @JsonProperty("shipment_id")
    private String shipmentId;

    @JsonProperty("awb_code")
    private String awbCode;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("courier_name")
    private String courierName;

    @JsonProperty("courier_id")
    private String courierId;

    private String status;

    @JsonProperty("pickup_scheduled_date")
    private String pickupScheduledDate;

    @JsonProperty("pickup_token_number")
    private String pickupTokenNumber;

    @JsonProperty("label_url")
    private String labelUrl;

    @JsonProperty("manifest_url")
    private String manifestUrl;
}
