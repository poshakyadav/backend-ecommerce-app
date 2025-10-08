package com.ecommerce.bkd.records;

import java.math.BigDecimal;

public record OrderItemDTO(
                Long id,
                Long productId,
                String productName,
                int quantity,
                BigDecimal unitPrice,
                BigDecimal discount,
                BigDecimal tax,
                BigDecimal priceAtPurchase,
                String selectedSize,
                String selectedColor,
                BigDecimal subtotal) {
}