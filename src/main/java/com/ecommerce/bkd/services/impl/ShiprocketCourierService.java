package com.ecommerce.bkd.services.impl;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ecommerce.bkd.dtos.shiprocket.*;
import com.ecommerce.bkd.exceptions.ShiprocketApiException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiprocketCourierService {

    private final ShiprocketApiService apiService;

   /**
     * Checks courier serviceability via Shiprocket API.
     *
     * @param pickup         the pickup postcode
     * @param delivery       the delivery postcode
     * @param weight         the total weight (in kg)
     * @param cod            true if Cash on Delivery (COD), false if Prepaid
     * @param orderId        (optional) your internal Shiprocket order_id (as String)
     * @param isReturn       (optional) true if this is a return order, false otherwise
     * @param declaredValue  (optional) required if isReturn=true (declared value of the order)
     * @param length         (optional) length of the package in cm
     * @param breadth        (optional) breadth of the package in cm
     * @param height         (optional) height of the package in cm
     * @return ShiprocketResponse<ServiceabilityData> (inside ServiceabilityData is
     *         List<CourierServiceability> under getData().getAvailableCourierCompanies())
     */
    @Cacheable(
    value = "courierServiceability",
    key =
    "#pickup + '-' + #delivery + '-' + " +
    "(#weight == null ? '' : #weight) + '-' + " +
    "(#cod == null ? '' : #cod) + '-' + " +
    "(#orderId == null ? '' : #orderId) + '-' + " +
    "(#isReturn == null ? '' : #isReturn) + '-' + " +
    "(#declaredValue == null ? '' : #declaredValue) + '-' + " +
    "(#length == null ? '' : #length) + '-' + " +
    "(#breadth == null ? '' : #breadth) + '-' + " +
    "(#height == null ? '' : #height)"
    )
    public ShiprocketResponse<ServiceabilityData> checkServiceability(
            String pickup,
            String delivery,
            Double weight,
            Boolean cod,
            String orderId,
            Boolean isReturn,
            BigDecimal declaredValue,
            Integer length,
            Integer breadth,
            Integer height
    ) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("pickup_postcode", pickup);
            body.put("delivery_postcode", delivery);

            if (orderId != null && !orderId.isBlank()) {
                // If order_id is provided, Shiprocket will reuse the dimensions
                body.put("order_id", orderId);

                // Optional- You may override stored dimensions if you still supply them:
                if (weight != null) body.put("weight", weight);
                if (length != null) body.put("length", length);
                if (breadth != null) body.put("breadth", breadth);
                if (height != null) body.put("height", height);
            } else {
                // When order_id is not provided, weight and cod are mandatory
                if (weight == null || cod == null) {
                    throw new IllegalArgumentException("Weight and COD are required when order_id is not provided.");
                }
                body.put("weight", weight);
                body.put("cod", cod ? 1 : 0);
                if (length != null) body.put("length", length);
                if (breadth != null) body.put("breadth", breadth);
                if (height != null) body.put("height", height);
            }

            if (isReturn != null) {
                body.put("is_return", isReturn ? 1 : 0);
                if (isReturn && declaredValue != null) {
                    body.put("declared_value", declaredValue);
                }
            }

            return apiService.post(
                    "/courier/serviceability",
                    body,
                    new ParameterizedTypeReference<ShiprocketResponse<ServiceabilityData>>() {}
            );
        } catch (Exception e) {
            log.error("Error checking serviceability: ", e);
            throw new ShiprocketApiException("Failed to check serviceability: " + e.getMessage(), e);
        }
    }

    /**
     * Overload for the common case where you don’t have orderId, isReturn, declaredValue, or dimensions.
     */
    public ShiprocketResponse<ServiceabilityData> checkServiceability(
            String pickup,
            String delivery,
            double weight,
            boolean cod
    ) {
        return checkServiceability(
            pickup, delivery,
            Double.valueOf(weight),
            Boolean.valueOf(cod),
            null,      // no orderId
            null,      // no isReturn
            null,      // no declaredValue
            null,      // no length
            null,      // no breadth
            null       // no height
        );
    }


     /**
     * Generates (or re‐assigns) an AWB for an existing shipment.
     *
     * According to Shiprocket docs, the request JSON looks like:
     * {
     *   "shipment_id": "16090281",
     *   "courier_id": 10
     * }
     *
     * The response from Shiprocket looks like:
     * {
     *   "awb_assign_status": 1,
     *   "response": {
     *     "data": {
     *        "courier_company_id": 142,
     *        "awb_code": "321055706540",
     *        ...
     *     }
     *   }
     * }
     *
     * We map that entire JSON into an AwbAssignWrapper.
     */
    public AwbAssignWrapper generateAWB(Long shipmentId, Integer courierId) {
        try {
            // Build the request body exactly as Shiprocket expects
            Map<String, Object> request = Map.of(
                "shipment_id", shipmentId,
                "courier_id",  courierId
            );

            // Call the `/courier/assign/awb` endpoint, deserializing into AwbAssignWrapper
            return apiService.post(
                "/courier/assign/awb",
                request,
                new ParameterizedTypeReference<AwbAssignWrapper>() {}
            );

        } catch (Exception e) {
            log.error("Error generating AWB: ", e);
            throw new ShiprocketApiException("Failed to generate AWB: " + e.getMessage(), e);
        }
    }


    /**
     * Generates (or retries) a pickup request for a single shipment.
     *
     * According to Shiprocket docs, request JSON:
     * {
     *   "shipment_id": [16091084]
     * }
     *
     * And response:
     * {
     *   "pickup_status": 1,
     *   "response": {
     *     "pickup_scheduled_date": "2021-12-10 12:39:54",
     *     "pickup_token_number": "Reference No: …",
     *     "status": 3,
     *     "others": "...",
     *     "pickup_generated_date": { … },
     *     "data": "Pickup is confirmed by Xpressbees …"
     *   }
     * }
     *
     * We map it into a PickupAssignWrapper.
     */
    public PickupAssignWrapper requestPickup(Long shipmentId, String pickupDate) {
        try {
            Map<String, Object> request = Map.of(
                // API expects an array of shipment IDs even if only one
                "shipment_id", List.of(shipmentId),
                // Optional: schedule for a future date
                "pickup_date", List.of(pickupDate)
            );

            return apiService.post(
                "/courier/generate/pickup",
                request,
                new ParameterizedTypeReference<PickupAssignWrapper>() {}
            );
        } catch (Exception e) {
            log.error("Error scheduling pickup: ", e);
            throw new ShiprocketApiException("Failed to schedule pickup: " + e.getMessage(), e);
        }
    }
}
