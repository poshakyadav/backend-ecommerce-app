package com.ecommerce.bkd.services;

import java.util.Map;

import com.ecommerce.bkd.dtos.wishlist.WishlistDTO;
import com.ecommerce.bkd.dtos.wishlist.WishlistItemRequestDTO;

public interface WishlistService {
    WishlistDTO addItemToWishlist(WishlistItemRequestDTO request, Long userId);
    WishlistDTO removeItemFromWishlist(Long wishlistItemId, Long userId);
    WishlistDTO getWishlist(Long userId);
    WishlistDTO moveItemToCart(Long wishlistItemId, Long userId, String selectedSize);
    WishlistDTO moveAllItemsToCart(Long userId, Map<Long, String> sizeSelections);
    void clearWishlist(Long userId);
}