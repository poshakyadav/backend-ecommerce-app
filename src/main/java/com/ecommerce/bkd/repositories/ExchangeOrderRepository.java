package com.ecommerce.bkd.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.bkd.models.ExchangeOrder;
import com.ecommerce.bkd.models.enums.ExchangeOrderStatus;

public interface ExchangeOrderRepository extends JpaRepository<ExchangeOrder, Long> {
    List<ExchangeOrder> findByOrderUserId(Long userId);
    List<ExchangeOrder> findByStatus(ExchangeOrderStatus status);
    Optional<ExchangeOrder> findByShiprocketExchangeShipmentId(String shipmentId);
    List<ExchangeOrder> findByRazorpayOrderId(String razorpayOrderId);
}