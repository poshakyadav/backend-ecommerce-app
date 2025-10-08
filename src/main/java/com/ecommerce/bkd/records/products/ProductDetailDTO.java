package com.ecommerce.bkd.records.products;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDTO {

    private Long id;
    private String brand;
    private ProductCategoryDTO category;
    private ProductVariantDTO defaultVariant;
    private List<ProductVariantDTO> variants;
    private Map<String, List<String>> availableAttributes;
    private BigDecimal averageRating;
    private Long totalRatings;
    private Map<Integer, Long> ratingDistribution;
    private List<ProductReviewDTO> reviews;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductCategoryDTO {
        private Long id;
        private String name;
    }
}