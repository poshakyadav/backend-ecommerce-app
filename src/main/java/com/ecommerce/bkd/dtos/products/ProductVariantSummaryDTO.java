package com.ecommerce.bkd.dtos.products;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Search Product related dto
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantSummaryDTO {
    private Long variantId;
    private Long productId;
    private String name; // Variant name, e.g., "Men's Crimson T-Shirt"
    private String imageUrl; // First image URL
    private BigDecimal price; // Total price of default size
    private String brand;
    private String categoryName;
}
