package com.ecommerce.bkd.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ecommerce.bkd.dtos.shiprocket.*;
import com.ecommerce.bkd.exceptions.ShiprocketApiException;
import com.ecommerce.bkd.models.Order;
import com.ecommerce.bkd.models.enums.OrderFulfillmentStatus;
import com.ecommerce.bkd.repositories.OrderRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiprocketTrackingService {

    private final ShiprocketApiService apiService;
    private final OrderRepository orderRepository;

    @Cacheable(value = "shipmentTracking", key = "#awbNumber", unless = "#result == null")
    public ShiprocketTracking trackByAWB(String awbNumber) {
        try {
            return apiService.get("/courier/track/awb/" + awbNumber, ShiprocketTracking.class);
        } catch (Exception e) {
            log.error("Error tracking shipment: ", e);
            throw new ShiprocketApiException("Failed to track shipment: " + e.getMessage());
        }
    }

    @Cacheable(value = "bulkTrackingCache", key = "#awbNumbers.toString()", unless = "#result == null")
    public Map<String, ShiprocketTracking> trackMultipleAWBs(List<String> awbNumbers) {
        try {
            Map<String, Object> request = Map.of("awbs", awbNumbers);
            return apiService.post("/courier/track/awbs",
                    request,
                    new ParameterizedTypeReference<Map<String, ShiprocketTracking>>() {
                    });
        } catch (Exception e) {
            log.error("Error tracking multiple shipments: ", e);
            throw new ShiprocketApiException("Failed to track multiple shipments: " + e.getMessage());
        }
    }

    @Cacheable(value = "orderTracking", key = "#orderId", unless = "#result == null")
    public ShiprocketTracking trackByOrderId(String orderId) {
        try {
            return apiService.get("/courier/track?order_id=" + orderId, ShiprocketTracking.class);
        } catch (Exception e) {
            log.error("Error tracking order: ", e);
            throw new ShiprocketApiException("Failed to track order: " + e.getMessage());
        }
    }

    @Cacheable(value = "shipmentTracking", key = "#shipmentId", unless = "#result == null")
    public ShiprocketTracking trackByShipmentId(Long shipmentId) {
        try {
            return apiService.get("/courier/track/shipment/" + shipmentId,
                    ShiprocketTracking.class);
        } catch (Exception e) {
            log.error("Error tracking shipment ID {}: ", shipmentId, e);
            throw new ShiprocketApiException("Failed to track shipment: " + e.getMessage());
        }
    }

    /**
     * Processes a tracking update.
     * 
     * @param event parsed webhook payload
     */
    public void handleWebhookUpdate(ShiprocketWebhookEvent event) {
        log.info("Processing Shiprocket webhook for AWB={} status={}",
                event.getAwb(), event.getCurrentStatus());

        // 1) Parse Shiprocket timestamp format "dd MM yyyy HH:mm:ss"
        LocalDateTime ts = LocalDateTime.parse(
                event.getCurrentTimestamp(),
                DateTimeFormatter.ofPattern("dd MM yyyy HH:mm:ss"));

        // 2) Find the order by AWB
        Order order = orderRepository
                .findByShiprocketAwbNumber(event.getAwb())
                .orElseThrow(() -> new ShiprocketApiException(
                        "Order not found for AWB " + event.getAwb()));

        // 3) Always record the raw status and timestamp (Or Update the domain fields)
        order.setShipmentStatus(event.getShipmentStatus());
        order.setLastTrackingUpdate(ts);

        // 4) Map courier statuses into the OrderStatus enum
        String cs = event.getCurrentStatus().toUpperCase();
        switch (cs) {
            case "SHIPPED" -> {
                order.setFulfillmentStatus(OrderFulfillmentStatus.SHIPPED);
            }
            case "IN TRANSIT" -> {
                order.setFulfillmentStatus(OrderFulfillmentStatus.IN_TRANSIT);
            }
            case "OUT FOR DELIVERY" -> {
                order.setFulfillmentStatus(OrderFulfillmentStatus.OUT_FOR_DELIVERY);
            }
            case "DELIVERED" -> {
                order.setFulfillmentStatus(OrderFulfillmentStatus.DELIVERED);
                order.setDeliveredAt(ts); // record delivery time for return window
            }
            case "UNDELIVERED" -> {
                order.setFulfillmentStatus(OrderFulfillmentStatus.UNDELIVERED);
            }
            case "RTO_INITIATED" -> {
                order.setFulfillmentStatus(OrderFulfillmentStatus.RTO_INITIATED);
            }
            case "RTO_DELIVERED" -> {
                order.setFulfillmentStatus(OrderFulfillmentStatus.RTO_DELIVERED);
            }
            default -> {
                // any other statuses you want to handle can go here
            }
        }

        // 5) Persist the changes
        orderRepository.save(order);

        log.info("Order {} updated to status={} at {}",
                order.getId(), event.getCurrentStatus(), ts);
    }

}
