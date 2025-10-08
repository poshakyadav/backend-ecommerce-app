package com.ecommerce.bkd.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.bkd.exceptions.ProductReviewException;
import com.ecommerce.bkd.models.ProductReview;
import com.ecommerce.bkd.records.products.ProductReviewDTO;
import com.ecommerce.bkd.repositories.ProductReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminProductReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductServiceImpl productService;

    @Transactional
    public ProductReviewDTO moderateReview(Long reviewId, ProductReview.ReviewStatus status) {
        ProductReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ProductReviewException("Review not found"));
        if (review.getStatus().equals(status)) {
            return mapToReviewDTO(review);
        }

        review.setStatus(status);
        review = reviewRepository.save(review);
        productService.updateProductRatingStats(review.getProduct().getId());
        return mapToReviewDTO(review);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        ProductReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ProductReviewException("Review not found"));
        review.getOrderItem().setReview(null);
        reviewRepository.delete(review);
        productService.updateProductRatingStats(review.getProduct().getId());
    }

    @Transactional(readOnly = true)
    public Page<ProductReviewDTO> getReviewsByStatus(ProductReview.ReviewStatus status, Pageable pageable) {
        Page<ProductReview> reviews = reviewRepository.findAllByStatusAndIsActive(status, true, pageable);
        return reviews.map(this::mapToReviewDTO);
    }

    private ProductReviewDTO mapToReviewDTO(ProductReview review) {
        return new ProductReviewDTO(
            review.getId(),
            review.getUser().getId(),
            review.getUser().getUsername(),
            review.getRating(),
            review.getReviewText(),
            review.getImageUrls(),
            review.getStatus().name(),
            review.getHelpfulVotes(),
            review.getUnhelpfulVotes(),
            review.getCreatedAt(),
            true
        );
    }
}