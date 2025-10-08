package com.ecommerce.bkd.dtos.wishlist;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.ecommerce.bkd.records.products.ProductDetailDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDTO {

    private Long id;
    private List<WishlistItemDTO> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WishlistItemDTO {
        private Long id;
        private ProductVariantDTO variant;
        private LocalDateTime addedAt;
        private boolean inStock;
        private boolean priceChanged;
        private BigDecimal currentPrice; // Current discounted price for comparison

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ProductVariantDTO {
            private Long id;
            private String sku;
            private String hsnCode;
            private String productName;
            private String brand;
            private String description;
            private List<String> imageUrls;
            private ProductDetailDTO.ProductCategoryDTO category;
            private Map<String, String> attributes;
        }
    }
}