package com.ecommerce.bkd.records.products;

import java.math.BigDecimal;
import java.util.List;

import com.ecommerce.bkd.dtos.products.VariantColorDTO;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductCreateDTO(
    String brand,
    @NotNull Long categoryId,
    @NotEmpty @Size(min = 1) List<VariantDTO> variants
) {
    public record VariantDTO(
        @NotBlank String sku,
        @NotBlank String name,
        @NotBlank String description,
        @NotNull VariantColorDTO color,
        @NotEmpty @Size(min = 1) List<SizePriceDTO> sizePrices,
        @NotNull @DecimalMin("0.0") BigDecimal length,
        @NotNull @DecimalMin("0.0") BigDecimal breadth,
        @NotNull @DecimalMin("0.0") BigDecimal height,
        @NotNull @DecimalMin("0.0") BigDecimal weight,
        List<String> imageKeys,
        
        boolean isNewArrival,
        boolean isTrendy,
        boolean isRecommended
    ) {
        public record SizePriceDTO(
            @NotBlank String size,
            @NotNull @DecimalMin("0.0") BigDecimal basePrice,
            @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal discount,
            @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal taxRate,
            @NotNull @Min(0) Integer stock,
            boolean isDefault
        ) {}
    }
}