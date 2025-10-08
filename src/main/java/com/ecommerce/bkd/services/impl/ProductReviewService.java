package com.ecommerce.bkd.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.bkd.exceptions.ProductReviewException;
import com.ecommerce.bkd.models.OrderItem;
import com.ecommerce.bkd.models.Product;
import com.ecommerce.bkd.models.ProductReview;
import com.ecommerce.bkd.models.ReviewHelpfulnessVote;
import com.ecommerce.bkd.records.products.ProductReviewDTO;
import com.ecommerce.bkd.records.products.ReviewRequestDTO;
import com.ecommerce.bkd.repositories.ExchangeOrderRepository;
import com.ecommerce.bkd.repositories.OrderItemRepository;
import com.ecommerce.bkd.repositories.ProductRepository;
import com.ecommerce.bkd.repositories.ProductReviewRepository;
import com.ecommerce.bkd.repositories.ReviewHelpfulnessVoteRepository;
import com.ecommerce.bkd.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductReviewService {

    private final ProductReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReviewHelpfulnessVoteRepository voteRepository;
    private final CloudinaryService cloudinaryService;

    private final ExchangeOrderRepository exchangeOrderRepository;

    private static final int MAX_REVIEW_IMAGES = 5;
    private static final int REVIEW_EDIT_WINDOW_DAYS = 30;

    @Transactional
    public ProductReviewDTO submitReview(ReviewRequestDTO request, Long userId, List<MultipartFile> images) {
        validateUser(userId);
        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
            .orElseThrow(() -> new ProductReviewException("Order item not found"));
        validateReviewEligibility(orderItem, userId);

        // Check for pending exchanges
        if (!orderItem.isReviewEligible()) {
            throw new ProductReviewException("Reviews are disabled for this item due to a pending exchange request.");
        }

        if (orderItem.getReview() != null) {
            throw new ProductReviewException("Review already submitted for this order item");
        }

        ProductReview review = new ProductReview();
        review.setOrderItem(orderItem);
        review.setUser(userRepository.findById(userId).orElseThrow());
        review.setProduct(orderItem.getVariant().getProduct());
        review.setRating(request.getRating());
        review.setReviewText(request.getReviewText());
        review.setStatus(ProductReview.ReviewStatus.PENDING);

        if (images != null && !images.isEmpty()) {
            if (images.size() > MAX_REVIEW_IMAGES) {
                throw new ProductReviewException("Maximum " + MAX_REVIEW_IMAGES + " images allowed");
            }
            review.setImageUrls(cloudinaryService.uploadImages(images, "reviews/" + orderItem.getVariant().getName()));
        }

        review = reviewRepository.save(review);
        orderItem.setReview(review);
        orderItemRepository.save(orderItem);
        updateProductRatingStats(review.getProduct());
        return mapToReviewDTO(review);
    }

    @Transactional
    public ProductReviewDTO updateReview(Long reviewId, ReviewRequestDTO request, Long userId, List<MultipartFile> images) {
        ProductReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ProductReviewException("Review not found"));
        validateReviewOwnership(review, userId);
        if (review.getCreatedAt().plusDays(REVIEW_EDIT_WINDOW_DAYS).isBefore(LocalDateTime.now())) {
            throw new ProductReviewException("Review edit window has expired");
        }
        if (!review.getStatus().equals(ProductReview.ReviewStatus.PENDING)) {
            throw new ProductReviewException("Only pending reviews can be edited");
        }

        review.setRating(request.getRating());
        review.setReviewText(request.getReviewText());
        if (images != null && !images.isEmpty()) {
            if (images.size() > MAX_REVIEW_IMAGES) {
                throw new ProductReviewException("Maximum " + MAX_REVIEW_IMAGES + " images allowed");
            }
            review.getImageUrls().clear();
            review.setImageUrls(cloudinaryService.uploadImages(images, "reviews/" + review.getOrderItem().getVariant().getName()));
        }

        review = reviewRepository.save(review);
        updateProductRatingStats(review.getProduct());
        return mapToReviewDTO(review);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        ProductReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ProductReviewException("Review not found"));
        validateReviewOwnership(review, userId);
        if (review.getCreatedAt().plusDays(REVIEW_EDIT_WINDOW_DAYS).isBefore(LocalDateTime.now())) {
            throw new ProductReviewException("Review deletion window has expired");
        }

        OrderItem orderItem = review.getOrderItem();
        orderItem.setReview(null);
        orderItemRepository.save(orderItem);
        reviewRepository.delete(review);
        updateProductRatingStats(review.getProduct());
    }

    @Transactional
    public void voteReviewHelpfulness(Long reviewId, Long userId, boolean isHelpful) {
        validateUser(userId);
        ProductReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ProductReviewException("Review not found"));
        if (review.getUser().getId().equals(userId)) {
            throw new ProductReviewException("Cannot vote on your own review");
        }

        Optional<ReviewHelpfulnessVote> existingVote = voteRepository.findByReviewIdAndUserId(reviewId, userId);
        if (existingVote.isPresent()) {
            ReviewHelpfulnessVote vote = existingVote.get();
            if (vote.isHelpful() == isHelpful) {
                return; // No change needed
            }
            // Reverse previous vote
            if (vote.isHelpful()) {
                review.setHelpfulVotes(review.getHelpfulVotes() - 1);
            } else {
                review.setUnhelpfulVotes(review.getUnhelpfulVotes() - 1);
            }
            vote.setHelpful(isHelpful);
        } else {
            ReviewHelpfulnessVote vote = new ReviewHelpfulnessVote();
            vote.setReview(review);
            vote.setUser(userRepository.findById(userId).orElseThrow());
            vote.setHelpful(isHelpful);
            voteRepository.save(vote);
        }

        if (isHelpful) {
            review.setHelpfulVotes(review.getHelpfulVotes() + 1);
        } else {
            review.setUnhelpfulVotes(review.getUnhelpfulVotes() + 1);
        }
        reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public Page<ProductReviewDTO> getProductReviews(Long productId, Integer ratingFilter, Pageable pageable) {
        Page<ProductReview> reviews = ratingFilter != null
            ? reviewRepository.findByProductIdAndStatusAndRatingAndIsActive(productId, ProductReview.ReviewStatus.APPROVED, ratingFilter, true, pageable)
            : reviewRepository.findByProductIdAndStatusAndIsActive(productId, ProductReview.ReviewStatus.APPROVED, true, pageable);
        return reviews.map(this::mapToReviewDTO);
    }

    private void validateUser(Long userId) {
        if (userId == null) {
            throw new ProductReviewException("User authentication required");
        }
        userRepository.findById(userId)
            .orElseThrow(() -> new ProductReviewException("User not found"));
    }

    private void validateReviewEligibility(OrderItem orderItem, Long userId) {
        if (!orderItem.getOrder().getUser().getId().equals(userId)) {
            throw new ProductReviewException("Unauthorized access to order item");
        }
        if (orderItem.getOrder().getActualDeliveryDate() == null) {
            throw new ProductReviewException("Order item not delivered yet");
        }
    }

    private void validateReviewOwnership(ProductReview review, Long userId) {
        if (!review.getUser().getId().equals(userId)) {
            throw new ProductReviewException("Unauthorized access to review");
        }
    }

    private void updateProductRatingStats(Product product) {
        List<ProductReview> reviews = reviewRepository.findAllByProductIdAndStatusAndIsActive(product.getId(), ProductReview.ReviewStatus.APPROVED, true);
        long totalRatings = reviews.size();
        BigDecimal averageRating = totalRatings > 0
            ? BigDecimal.valueOf(reviews.stream().mapToInt(ProductReview::getRating).average().orElse(0.0))
                .setScale(1, BigDecimal.ROUND_HALF_UP)
            : BigDecimal.ZERO;

        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            ratingDistribution.put(i, reviews.stream().filter(r -> r.getRating() == rating).count());
        }

        product.setAverageRating(averageRating);
        product.setTotalRatings(totalRatings);
        product.setRatingDistribution(ratingDistribution);
        productRepository.save(product);
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
            true // Verified purchase (since tied to OrderItem)
        );
    }
}