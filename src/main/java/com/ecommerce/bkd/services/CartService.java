package com.ecommerce.bkd.services;

import com.ecommerce.bkd.records.cart.CartDTO;
import com.ecommerce.bkd.records.cart.CartItemRequestDTO;

public interface CartService {
    CartDTO addItemToCart(CartItemRequestDTO request, Long userId, String sessionId);
    CartDTO updateItemQuantity(Long cartItemId, Integer quantity, Long userId);
    CartDTO removeItemFromCart(Long cartItemId, Long userId);
    CartDTO getCart(Long userId, String sessionId);
    CartDTO mergeGuestCart(Long userId, String sessionId);
    void clearCart(Long userId);
}
