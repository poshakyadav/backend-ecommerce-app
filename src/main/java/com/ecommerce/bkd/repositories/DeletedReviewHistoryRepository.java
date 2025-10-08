package com.ecommerce.bkd.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.bkd.models.DeletedReviewHistory;

public interface DeletedReviewHistoryRepository extends JpaRepository<DeletedReviewHistory, Long> {
    Optional<DeletedReviewHistory> findByReviewIdAndExchangeOrderId(Long reviewId, Long exchangeOrderId);

    Optional<DeletedReviewHistory> findByExchangeOrderId(Long exchangeOrderId);

}