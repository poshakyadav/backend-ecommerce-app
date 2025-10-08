package com.ecommerce.bkd.controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.bkd.dtos.wishlist.WishlistDTO;
import com.ecommerce.bkd.dtos.wishlist.WishlistItemRequestDTO;
import com.ecommerce.bkd.exceptions.WishlistException;
import com.ecommerce.bkd.models.User;
import com.ecommerce.bkd.records.wishlist.WishlistSizeSelectionDTO;
import com.ecommerce.bkd.services.impl.WishlistServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistServiceImpl wishlistService;

    @PostMapping("/items")
    public ResponseEntity<WishlistDTO> addItemToWishlist(
        @Valid @RequestBody WishlistItemRequestDTO request,
        Authentication authentication) {
        Long userId = getUserId(authentication);
        WishlistDTO wishlistDTO = wishlistService.addItemToWishlist(request, userId);
        return ResponseEntity.ok(wishlistDTO);
    }

    @DeleteMapping("/items/{wishlistItemId}")
    public ResponseEntity<WishlistDTO> removeItemFromWishlist(
        @PathVariable Long wishlistItemId,
        Authentication authentication) {
        Long userId = getUserId(authentication);
        WishlistDTO wishlistDTO = wishlistService.removeItemFromWishlist(wishlistItemId, userId);
        return ResponseEntity.ok(wishlistDTO);
    }

    @GetMapping
    public ResponseEntity<WishlistDTO> getWishlist(Authentication authentication) {
        Long userId = getUserId(authentication);
        WishlistDTO wishlistDTO = wishlistService.getWishlist(userId);
        return ResponseEntity.ok(wishlistDTO);
    }

    @PostMapping("/items/{wishlistItemId}/move-to-cart")
    public ResponseEntity<WishlistDTO> moveItemToCart(
        @PathVariable Long wishlistItemId,
        @Valid @RequestBody WishlistSizeSelectionDTO sizeSelection,
        Authentication authentication) {
        Long userId = getUserId(authentication);
        WishlistDTO wishlistDTO = wishlistService.moveItemToCart(wishlistItemId, userId, sizeSelection.selectedSize());
        return ResponseEntity.ok(wishlistDTO);
    }

    @PostMapping("/move-all-to-cart")
    public ResponseEntity<WishlistDTO> moveAllItemsToCart(
        @Valid @RequestBody WishlistSizeSelectionDTO sizeSelection,
        Authentication authentication) {
        Long userId = getUserId(authentication);
        WishlistDTO wishlistDTO = wishlistService.moveAllItemsToCart(userId, sizeSelection.sizeSelections());
        return ResponseEntity.ok(wishlistDTO);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearWishlist(Authentication authentication) {
        Long userId = getUserId(authentication);
        wishlistService.clearWishlist(userId);
        return ResponseEntity.noContent().build();
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null) {
            throw new WishlistException("User authentication required");
        }
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}