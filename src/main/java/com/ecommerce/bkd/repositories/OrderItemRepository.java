package com.ecommerce.bkd.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.bkd.models.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    boolean existsByVariantId(Long variantId);

    boolean existsByVariantProductId(Long productId);
}
