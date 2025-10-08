package com.ecommerce.bkd.dtos.wishlist;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WishlistItemRequestDTO {
    @NotNull
    private Long variantId;
}