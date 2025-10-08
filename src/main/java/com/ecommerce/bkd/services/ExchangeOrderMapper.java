package com.ecommerce.bkd.services;

import java.math.BigDecimal;

import com.ecommerce.bkd.dtos.shiprocket.ShiprocketExchangeOrderRequest;
import com.ecommerce.bkd.models.ExchangeOrder;
import com.ecommerce.bkd.records.exchangeOrder.ExchangeOrderDTO;

public interface ExchangeOrderMapper {
    ExchangeOrderDTO toExchangeOrderDTO(ExchangeOrder eo);
    ExchangeOrderDTO toExchangeOrderDTO(ExchangeOrder eo, String razorpayOrderId, BigDecimal paymentAmount, String currency);
    ShiprocketExchangeOrderRequest toShiprocketExchangeOrderRequest(ExchangeOrder eo);
}