package com.ecommerce.bkd.dtos.products;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VariantUpdateDTO {
    @NotBlank
    private String sku;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull
    private VariantColorDTO color;
    @NotEmpty
    @Size(min = 1)
    private List<SizePriceDTO> sizePrices;
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal length;
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal breadth;
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal height;
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal weight;

    @JsonProperty("isNewArrival")
    private boolean isNewArrival = false;

    @JsonProperty("isTrendy")
    private boolean isTrendy = false;

    @JsonProperty("isRecommended")
    private boolean isRecommended = false;

    @Data
    public static class VariantColorDTO {
        @NotBlank
        private String name;
        @NotBlank
        @Pattern(regexp = "#[0-9A-Fa-f]{6}")
        private String hexCode;
    }

    @Data
    public static class SizePriceDTO {
        @NotBlank
        private String size;
        @NotNull
        @DecimalMin("0.0")
        private BigDecimal basePrice;
        @DecimalMin("0.0")
        @DecimalMax("1.0")
        private BigDecimal discount;
        @DecimalMin("0.0")
        @DecimalMax("1.0")
        private BigDecimal taxRate;
        @NotNull
        @Min(0)
        private Integer stock;
        private boolean isDefault;
    }
}