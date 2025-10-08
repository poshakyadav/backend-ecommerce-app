package com.ecommerce.bkd.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.bkd.exceptions.ProductReviewException;
import com.ecommerce.bkd.models.User;
import com.ecommerce.bkd.records.products.ProductReviewDTO;
import com.ecommerce.bkd.records.products.ReviewRequestDTO;
import com.ecommerce.bkd.services.impl.ProductReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewService reviewService;

    @PostMapping
    public ResponseEntity<ProductReviewDTO> submitReview(
            @Valid @RequestPart("review") ReviewRequestDTO request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        ProductReviewDTO reviewDTO = reviewService.submitReview(request, userId, images);
        return ResponseEntity.ok(reviewDTO);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ProductReviewDTO> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestPart("review") ReviewRequestDTO request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        ProductReviewDTO reviewDTO = reviewService.updateReview(reviewId, request, userId, images);
        return ResponseEntity.ok(reviewDTO);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{reviewId}/vote")
    public ResponseEntity<Void> voteReviewHelpfulness(
            @PathVariable Long reviewId,
            @RequestParam boolean isHelpful,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        reviewService.voteReviewHelpfulness(reviewId, userId, isHelpful);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<ProductReviewDTO>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(required = false) Integer rating,
            Pageable pageable) {
        Page<ProductReviewDTO> reviews = reviewService.getProductReviews(productId, rating, pageable);
        return ResponseEntity.ok(reviews);
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null) {
            throw new ProductReviewException("User authentication required");
        }
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}