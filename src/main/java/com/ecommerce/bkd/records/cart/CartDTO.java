package com.ecommerce.bkd.records.cart;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.ecommerce.bkd.records.products.ProductDetailDTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {

    private Long id;
    private List<CartItemDTO> items;
    private BigDecimal totalBasePrice;
    private BigDecimal totalDiscountAmount;
    private BigDecimal totalTaxAmount;
    private BigDecimal totalPrice; // Added to represent sum of discounted prices
    private BigDecimal totalFinalPrice;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemDTO {
        private Long id;
        private ProductVariantDTO variant;
        private Integer quantity;
        private String selectedSize;
        private BigDecimal basePrice;
        private BigDecimal discount;
        private BigDecimal discountAmount;
        private BigDecimal discountedPrice;
        private BigDecimal taxRate;
        private BigDecimal taxAmount;
        private BigDecimal totalPrice;
        private BigDecimal finalPrice;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ProductVariantDTO {
            private Long id;
            private String sku;
            private String productName;
            private List<String> imageUrls;
            private ProductDetailDTO.ProductCategoryDTO category;
            private Map<String, String> attributes;
        }
    }
}