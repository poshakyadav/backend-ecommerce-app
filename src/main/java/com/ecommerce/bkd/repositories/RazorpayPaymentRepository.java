package com.ecommerce.bkd.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.bkd.models.Order;
import com.ecommerce.bkd.models.RazorpayPayment;

public interface RazorpayPaymentRepository extends JpaRepository<RazorpayPayment, Long> {

    /**
     * Find the RazorpayPayment record for a given Order.
     */
    Optional<RazorpayPayment> findByOrder(Order order);
}
