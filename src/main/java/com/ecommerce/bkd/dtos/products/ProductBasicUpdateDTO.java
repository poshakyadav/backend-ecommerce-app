package com.ecommerce.bkd.dtos.products;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductBasicUpdateDTO {
    @NotNull
    private Long id;
    @NotBlank
    private String brand;
    @NotNull
    private Long categoryId;
}