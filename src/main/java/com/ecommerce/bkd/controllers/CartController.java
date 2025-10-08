package com.ecommerce.bkd.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.bkd.dtos.ApiResponse;
import com.ecommerce.bkd.models.User;
import com.ecommerce.bkd.records.cart.CartDTO;
import com.ecommerce.bkd.records.cart.CartItemRequestDTO;
import com.ecommerce.bkd.services.impl.CartServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartServiceImpl cartService;

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartDTO>> addItemToCart(
            @Valid @RequestBody CartItemRequestDTO request,
            Authentication authentication,
            @RequestHeader(value = "X-Session-ID", required = false) String sessionId) {
        Long userId = authentication != null ? getUserId(authentication) : null;
        CartDTO cartDTO = cartService.addItemToCart(request, userId, sessionId);
        ApiResponse<CartDTO> apiResponse = new ApiResponse<>(true, "Item added to cart successfully", cartDTO);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartDTO>> updateItemQuantity(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity,
            Authentication authentication) {
        Long userId = authentication != null ? getUserId(authentication) : null;
        CartDTO cartDTO = cartService.updateItemQuantity(cartItemId, quantity, userId);
        ApiResponse<CartDTO> apiResponse = new ApiResponse<>(true, "Item quantity updated successfully", cartDTO);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartDTO>> removeItemFromCart(
            @PathVariable Long cartItemId,
            Authentication authentication) {
        Long userId = authentication != null ? getUserId(authentication) : null;
        CartDTO cartDTO = cartService.removeItemFromCart(cartItemId, userId);
        ApiResponse<CartDTO> apiResponse = new ApiResponse<>(true, "Item removed from cart successfully", cartDTO);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartDTO>> getCart(
            Authentication authentication,
            @RequestHeader(value = "X-Session-ID", required = false) String sessionId) {
        Long userId = authentication != null ? getUserId(authentication) : null;
        CartDTO cartDTO = cartService.getCart(userId, sessionId);
        ApiResponse<CartDTO> apiResponse = new ApiResponse<>(true, "Cart retrieved successfully", cartDTO);
        System.out.println("Retrieved cart for userId: " + userId + ", sessionId: " + sessionId);
        System.out.println("Cart details: " + cartDTO);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/merge")
    public ResponseEntity<ApiResponse<CartDTO>> mergeGuestCart(
            @RequestHeader("X-Session-ID") String sessionId,
            Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("User must be authenticated to merge cart");
        }
        Long userId = getUserId(authentication);
        CartDTO cartDTO = cartService.mergeGuestCart(userId, sessionId);
        ApiResponse<CartDTO> apiResponse = new ApiResponse<>(true, "Guest cart merged successfully", cartDTO);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/clear")
    public ResponseEntity<ApiResponse<String>> clearCart(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("User must be authenticated to clear cart");
        }
        Long userId = getUserId(authentication);
        cartService.clearCart(userId);
        ApiResponse<String> apiResponse = new ApiResponse<>(true, "Cart cleared successfully", null);
        return ResponseEntity.ok(apiResponse);
    }

    private Long getUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}