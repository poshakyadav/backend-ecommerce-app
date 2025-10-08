package com.ecommerce.bkd.dtos.products;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import com.ecommerce.bkd.models.enums.ProductAvailabilityStatus;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class ProductCreateRequest {
    @NotBlank
    @Size(max = 100)
    private String name;

    private String description;

    private String brand;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal basePrice;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private BigDecimal discount;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private BigDecimal taxRate;

    @Min(0)
    private Integer stock;

    private ProductDimensionsDTO dimensions;

    private String metaTitle;

    private String metaDescription;

    private Set<String> metaKeywords;

    @NotBlank
    private String currency; // e.g., "USD", "INR", etc.

    private ProductAvailabilityStatus availabilityStatus;

    private Set<String> tags;

    private Map<String, String> specifications;

    private Map<String, String> productAttributes;

    private Long categoryId;

    private String sku;

    private String hsnCode;
}