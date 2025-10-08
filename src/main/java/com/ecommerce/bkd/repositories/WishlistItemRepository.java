package com.ecommerce.bkd.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.bkd.models.WishlistItem;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    Optional<WishlistItem> findByWishlistIdAndVariantId(Long wishlistId, Long variantId);

    void deleteByWishlistId(Long wishlistId);

}
