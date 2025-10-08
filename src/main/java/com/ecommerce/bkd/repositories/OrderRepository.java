package com.ecommerce.bkd.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.bkd.models.Order;
import com.ecommerce.bkd.models.enums.OrderFulfillmentStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
       List<Order> findByUserId(Long userId);

       List<Order> findByFulfillmentStatus(OrderFulfillmentStatus status);

       Optional<Order> findByRazorpayPaymentId(String razorpayPaymentId);

       Optional<Order> findByShiprocketAwbNumber(String awbNumber);

       /**
        * Find an order by its id _and_ the owning user's id,
        * so that UserOrderService.findOwnedOrder(...) compiles.
        */
       Optional<Order> findByIdAndUserId(Long id, Long userId);

}
