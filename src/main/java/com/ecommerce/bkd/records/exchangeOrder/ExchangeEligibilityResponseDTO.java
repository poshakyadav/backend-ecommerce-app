package com.ecommerce.bkd.records.exchangeOrder;

import java.time.LocalDateTime;
import java.util.List;

public record ExchangeEligibilityResponseDTO(
    boolean eligible,
    String message,
    LocalDateTime deliveryDate,
    List<OrderItemDTO> items
) {
    public record OrderItemDTO(
        Long id,
        String productName,
        String sku,
        String variantDescription,
        List<String> imageUrls
    ) {}
}