package com.ecommerce.bkd.dtos.shiprocket;

import lombok.Data;
import java.util.List;

@Data
public class ShiprocketTracking {
    private TrackingData trackingData;

    @Data
    public static class TrackingData {
        private List<ShipmentTrack> shipmentTrack;
        private String currentStatus;
        private String awb;
        private String orderStatus;
    }

    @Data
    public static class ShipmentTrack {
        private String date;
        private String activity;
        private String location;
        private String status;
        private String srStatus;
        private String srStatusLabel;
    }
}
