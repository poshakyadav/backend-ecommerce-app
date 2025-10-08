package com.ecommerce.bkd.records.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CartItemRequestDTO(
    @NotNull Long variantId,
    @NotNull @Min(1) Integer quantity,
    @NotBlank String selectedSize
) {}