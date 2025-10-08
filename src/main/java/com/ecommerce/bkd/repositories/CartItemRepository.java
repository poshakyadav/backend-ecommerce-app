package com.ecommerce.bkd.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.bkd.models.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.variant.id = :variantId")
    Optional<CartItem> findByCartIdAndVariantId(@Param("cartId") Long cartId, @Param("variantId") Long variantId);

    /**
     * Finds a cart item by cart ID, variant ID, and selected size.
     *
     * @param cartId      the ID of the cart
     * @param variantId   the ID of the product variant
     * @param selectedSize the selected size of the variant
     * @return an Optional containing the cart item if found
     */
    Optional<CartItem> findByCartIdAndVariantIdAndSelectedSize(Long cartId, Long variantId, String selectedSize);

    /**
     * Deletes all cart items associated with a cart ID.
     *
     * @param cartId the ID of the cart
     */
    void deleteByCartId(Long cartId);
}