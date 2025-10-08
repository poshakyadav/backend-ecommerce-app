package com.ecommerce.bkd.services.impl;

import org.springframework.stereotype.Service;

import com.ecommerce.bkd.models.Order;
import com.ecommerce.bkd.models.enums.OrderPaymentStatus;
import com.ecommerce.bkd.repositories.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentErrorHandlingService {
    
    private final OrderRepository orderRepository;
    
    public void handlePaymentError(Order order, String errorCode, String errorMessage) {
        log.error("Payment error for order {}: {} - {}", order.getId(), errorCode, errorMessage);
        
        order.setOrderPaymentStatus(OrderPaymentStatus.PAYMENT_FAILED);
        order.setPaymentStatus("FAILED");
        order.setErrorCode(errorCode);
        order.setErrorMessage(errorMessage);
        
        orderRepository.save(order);
        // Implement notification logic here
    }
}
