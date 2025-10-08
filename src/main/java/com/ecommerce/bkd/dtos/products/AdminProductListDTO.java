package com.ecommerce.bkd.dtos.products;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class AdminProductListDTO {
    private Long id;
    private String brand;
    private ProductCategoryDTO category;
    private boolean active;
    private List<VariantDTO> variants;

    @Data
    @AllArgsConstructor
    public static class ProductCategoryDTO {
        private Long id;
        private String name;
    }

    @Data
    public static class VariantDTO {
        private Long id;
        private String sku;
        private String name;
        private String description;
        private List<String> imageUrls;
        private VariantColorDTO color;
        private List<SizePriceDTO> sizePrices;
        private boolean active;
        private BigDecimal length;
        private BigDecimal breadth;
        private BigDecimal height;
        private BigDecimal weight;

        private boolean isNewArrival;
        private boolean isTrendy;
        private boolean isRecommended;
        private Long viewCount;
        private Long salesCount;
    }

    @Data
    @AllArgsConstructor
    public static class SizePriceDTO {
        private Long id;
        private String size;
        private BigDecimal basePrice;
        private BigDecimal discount;
        private BigDecimal discountAmount;
        private BigDecimal discountedPrice;
        private BigDecimal taxRate;
        private BigDecimal taxAmount;
        private BigDecimal totalPrice;
        private BigDecimal finalPrice;
        private int stock;
        private boolean isDefault;
    }
}
