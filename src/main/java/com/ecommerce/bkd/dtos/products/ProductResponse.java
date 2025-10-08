package com.ecommerce.bkd.dtos.products;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ecommerce.bkd.dtos.ProductImageDTO;
import com.ecommerce.bkd.models.enums.ProductAvailabilityStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private String brand;
    private BigDecimal basePrice;
    private Double discount;
    private BigDecimal discountedPrice;
    private Integer stock;
    private Double rating;
    private ProductDimensionsDTO dimensions;
    private String metaTitle;
    private String metaDescription;
    private Set<String> metaKeywords;
    private String currency;
    private ProductAvailabilityStatus availabilityStatus;
    private Set<String> tags;
    private Map<String, String> specifications;
    private Map<String, String> productAttributes;
    private CategoryDTO category;
    private List<ProductImageDTO> images;
    private String sku;
    private String hsnCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal discountAmount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal totalPrice;
    private BigDecimal finalPrice;
}
