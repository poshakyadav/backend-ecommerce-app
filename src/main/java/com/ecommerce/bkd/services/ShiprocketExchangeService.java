package com.ecommerce.bkd.services;

import org.springframework.stereotype.Service;

import com.ecommerce.bkd.dtos.shiprocket.ShiprocketExchangeOrderRequest;
import com.ecommerce.bkd.dtos.shiprocket.ShiprocketExchangeOrderResponse;

@Service
public interface ShiprocketExchangeService {
    ShiprocketExchangeOrderResponse createExchangeOrder(ShiprocketExchangeOrderRequest request);
    void generateExchangeAwb(String shipmentId, String courierId, boolean isExchange);
}