package com.ecommerce.bkd.services.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ecommerce.bkd.dtos.shiprocket.*;
import com.ecommerce.bkd.exceptions.ShiprocketApiException;
import com.ecommerce.bkd.utils.QueryUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiprocketShipmentService {

    private final ShiprocketApiService apiService;

    /**
     * Retrieve all shipments with optional filters
     */
    public List<ShiprocketShipment> getAllShipments(ShipmentListRequest request) {
        Map<String, Object> params = new HashMap<>();

        Optional.ofNullable(request.getSort()).ifPresent(v -> params.put("sort", v));
        Optional.ofNullable(request.getSortBy()).ifPresent(v -> params.put("sort_by", v));
        Optional.ofNullable(request.getFilter()).ifPresent(v -> params.put("filter", v));
        Optional.ofNullable(request.getFilterBy()).ifPresent(v -> params.put("filter_by", v));

        return apiService.get("/shipments?" + QueryUtils.buildQueryParams(params),
                new ParameterizedTypeReference<List<ShiprocketShipment>>() {
                });
    }

    /**
     * Get details of a specific shipment by its ID
     * (GET /shipments/{shipment_id})
     */
    public ShiprocketShipment getShipmentById(Long shipmentId) {
        try {
            return apiService.get("/shipments/" + shipmentId, ShiprocketShipment.class);
        } catch (Exception e) {
            log.error("Error fetching shipment {}: ", shipmentId, e);
            throw new ShiprocketApiException("Failed to fetch shipment details: " + e.getMessage());
        }
    }

    /**
     * Cancel one or more AWBs before pickup (before out-for-pickup)
     */
    public ShiprocketResponse<?> cancelShipmentsByAwbs(List<String> awbNumbers) {
        try {
            return apiService.post(
                    "/orders/cancel/shipment/awbs",
                    Map.of("awbs", awbNumbers),
                    new ParameterizedTypeReference<ShiprocketResponse<?>>() {
                    });
        } catch (Exception e) {
            log.error("Error cancelling shipments: ", e);
            throw new ShiprocketApiException("Failed to cancel shipments: " + e.getMessage());
        }
    }

}
