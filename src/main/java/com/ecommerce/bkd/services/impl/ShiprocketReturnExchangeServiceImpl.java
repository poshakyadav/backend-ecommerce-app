package com.ecommerce.bkd.services.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.ecommerce.bkd.dtos.shiprocket.ReturnOrderRequest;
import com.ecommerce.bkd.dtos.shiprocket.ShiprocketReturnOrderResponse;
import com.ecommerce.bkd.dtos.shiprocket.ShiprocketReturnOrdersResponse;
import com.ecommerce.bkd.exceptions.ShiprocketApiException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiprocketReturnExchangeServiceImpl {

    private final ShiprocketApiService apiService;

    /**
     * Create a return order
     */
    public ShiprocketReturnOrderResponse createReturnOrder(ReturnOrderRequest request) {
        try {
            return apiService.post("/orders/create/return", request, ShiprocketReturnOrderResponse.class);
        } catch (Exception e) {
            log.error("Error creating return order: ", e);
            throw new ShiprocketApiException("Failed to create return order: " + e.getMessage());
        }
    }

    /**
     * Create an exchange order
     */
    // public ShiprocketExchangeOrderResponse createExchangeOrder(ExchangeOrderRequest request) {
    //     try {
    //         return apiService.post("/orders/create/exchange", request, ShiprocketExchangeOrderResponse.class);
    //     } catch (Exception e) {
    //         log.error("Error creating exchange order: ", e);
    //         throw new ShiprocketApiException("Failed to create exchange order: " + e.getMessage());
    //     }
    // }

    /**
     * Get all return orders
     */
    public ShiprocketReturnOrdersResponse getAllReturnOrders() {
        try {
            return apiService.get("/orders/processing/return", ShiprocketReturnOrdersResponse.class);
        } catch (Exception e) {
            log.error("Error fetching return orders: ", e);
            throw new ShiprocketApiException("Failed to fetch return orders: " + e.getMessage());
        }
    }

    /**
     * Generate AWB for a return shipment
     */
    public void generateReturnAwb(String shipmentId, String courierId, String status, boolean isReturn) {
        try {
            Map<String, Object> request = Map.of(
                    "shipment_id", shipmentId,
                    "courier_id", courierId,
                    "status", status,
                    "is_return", isReturn);
            apiService.post("/courier/assign/awb", request, Void.class); // No response body
        } catch (Exception e) {
            log.error("Error generating return AWB: ", e);
            throw new ShiprocketApiException("Failed to generate return AWB: " + e.getMessage());
        }
    }
}
