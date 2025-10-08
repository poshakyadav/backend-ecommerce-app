package com.ecommerce.bkd.dtos.products;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class ProductVariantWithPriceDTO {
    private Long variantId;
    private Long productId;
    private String name;
    private List<String> imageUrls;
    private BigDecimal totalPrice;
    private String brand;
    private String categoryName;

    public ProductVariantWithPriceDTO(Long variantId, Long productId, String name, List<String> imageUrls, BigDecimal totalPrice, String brand, String categoryName) {
        this.variantId = variantId;
        this.productId = productId;
        this.name = name;
        this.imageUrls = imageUrls;
        this.totalPrice = totalPrice;
        this.brand = brand;
        this.categoryName = categoryName;
    }
}
