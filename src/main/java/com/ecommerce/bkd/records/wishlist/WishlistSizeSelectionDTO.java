package com.ecommerce.bkd.records.wishlist;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record WishlistSizeSelectionDTO(
    @NotBlank String selectedSize, // For single item move
    @NotEmpty Map<Long, String> sizeSelections // For moving all items
) {}