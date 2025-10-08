package com.ecommerce.bkd.records.products;

import java.math.BigDecimal;
import java.util.List;

import com.ecommerce.bkd.dtos.products.VariantColorDTO;

public record ProductVariantDTO(
    Long id,
    String sku,
    String name,
    String description,
    List<String> imageUrls,
    VariantColorDTO color,
    List<SizePriceDTO> sizePrices,
    BigDecimal length,
    BigDecimal breadth,
    BigDecimal height,
    BigDecimal weight,

    boolean isNewArrival,
    boolean isTrendy,
    boolean isRecommended,
    Long viewCount,
    Long salesCount
) {
    public record SizePriceDTO(
        Long id,
        String size,
        BigDecimal basePrice,
        BigDecimal discount,
        BigDecimal discountAmount,
        BigDecimal discountedPrice,
        BigDecimal taxRate,
        BigDecimal taxAmount,
        BigDecimal totalPrice,
        BigDecimal finalPrice,
        Integer stock,
        boolean isDefault
    ) {}
}