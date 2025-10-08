package com.ecommerce.bkd.services.impl;

import org.springframework.stereotype.Service;

import com.ecommerce.bkd.dtos.shiprocket.ShiprocketExchangeOrderRequest;
import com.ecommerce.bkd.dtos.shiprocket.ShiprocketExchangeOrderResponse;
import com.ecommerce.bkd.services.ShiprocketExchangeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShiprocketExchangeServiceImpl implements ShiprocketExchangeService {

    private final ShiprocketApiService apiService;

    @Override
    public ShiprocketExchangeOrderResponse createExchangeOrder(ShiprocketExchangeOrderRequest request) {
        return apiService.post(
            "/v1/external/exchange-orders",
            request,
            ShiprocketExchangeOrderResponse.class
        );
    }

    @Override
    public void generateExchangeAwb(String shipmentId, String courierId, boolean isExchange) {
        String path = "/v1/external/courier/generate/awb?shipment_id=" + shipmentId +
            (courierId != null ? "&courier_id=" + courierId : "") + "&is_exchange=" + isExchange;
        apiService.post(path, new Object(), Void.class);
    }
}