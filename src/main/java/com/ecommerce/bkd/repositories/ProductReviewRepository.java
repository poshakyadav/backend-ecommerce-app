package com.ecommerce.bkd.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.bkd.models.ProductReview;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    Optional<ProductReview> findByOrderItemId(Long orderItemId);
    Page<ProductReview> findByProductIdAndStatus(Long productId, ProductReview.ReviewStatus status, Pageable pageable);
    Page<ProductReview> findByProductIdAndStatusAndRating(Long productId, ProductReview.ReviewStatus status, Integer rating, Pageable pageable);
    Long countByProductIdAndStatus(Long productId, ProductReview.ReviewStatus status);
    Page<ProductReview> findAllByStatus(ProductReview.ReviewStatus status, Pageable pageable);
    List<ProductReview> findAllByProductIdAndStatus(Long productId, ProductReview.ReviewStatus status);

    Page<ProductReview> findByProductIdAndStatusAndIsActive(Long productId, ProductReview.ReviewStatus status, boolean isActive, Pageable pageable);
    Page<ProductReview> findByProductIdAndStatusAndRatingAndIsActive(Long productId, ProductReview.ReviewStatus status, Integer rating, boolean isActive, Pageable pageable);
    List<ProductReview> findAllByProductIdAndStatusAndIsActive(Long productId, ProductReview.ReviewStatus status, boolean isActive);
    Page<ProductReview> findAllByStatusAndIsActive(ProductReview.ReviewStatus status, boolean isActive, Pageable pageable);

}