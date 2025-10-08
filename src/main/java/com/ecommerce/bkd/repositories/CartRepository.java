package com.ecommerce.bkd.repositories;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.bkd.models.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // @Query("SELECT c FROM Cart c WHERE c.user.id = :userId AND c.items IS NOT EMPTY")
    // Optional<Cart> findByUserId(@Param("userId") Long userId);

    Optional<Cart> findByUserId(Long userId);

    // @Query("SELECT c FROM Cart c WHERE c.sessionId = :sessionId AND c.items IS NOT EMPTY")
    // Optional<Cart> findBySessionId(@Param("sessionId") String sessionId);

    Optional<Cart> findBySessionId(String sessionId);

    void deleteByUserIsNullAndLastUpdatedBefore(LocalDateTime threshold);
}