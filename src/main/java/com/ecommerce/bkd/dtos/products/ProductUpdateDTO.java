package com.ecommerce.bkd.dtos.products;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProductUpdateDTO(
    @NotNull Long id,
    @NotBlank String brand,
    @NotNull Long categoryId,
    List<NewVariantDTO> newVariants
) {
    public record NewVariantDTO(
        @NotBlank String name,
        @NotBlank String sku,
        @NotBlank String description,
        @NotNull VariantColorDTO color,
        @NotEmpty @Size(min = 1) List<SizePriceDTO> sizePrices,
        @NotNull @DecimalMin("0.0") BigDecimal length,
        @NotNull @DecimalMin("0.0") BigDecimal breadth,
        @NotNull @DecimalMin("0.0") BigDecimal height,
        @NotNull @DecimalMin("0.0") BigDecimal weight,
        
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

    public record VariantColorDTO(
        Long id,
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "#[0-9A-Fa-f]{6}") String hexCode
    ) {}
}