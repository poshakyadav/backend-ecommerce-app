package com.ecommerce.bkd.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.bkd.models.ReturnOrder;
import com.ecommerce.bkd.models.enums.OrderReturnStatus;

@Repository
public interface ReturnOrderRepository extends JpaRepository<ReturnOrder, Long> {
    List<ReturnOrder> findByReturnStatus(OrderReturnStatus status);

    Optional<ReturnOrder> findByOrderId(Long orderId);

    Optional<ReturnOrder> findByShiprocketReturnAwbCode(String awb);
}