package com.ecommerce.bkd.controllers.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.bkd.models.ProductReview;
import com.ecommerce.bkd.records.products.ProductReviewDTO;
import com.ecommerce.bkd.services.impl.AdminProductReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductReviewController {

    private final AdminProductReviewService adminReviewService;

    @PutMapping("/{reviewId}/moderate")
    public ResponseEntity<ProductReviewDTO> moderateReview(
            @PathVariable Long reviewId,
            @RequestParam String status) {
        ProductReview.ReviewStatus reviewStatus = ProductReview.ReviewStatus.valueOf(status.toUpperCase());
        ProductReviewDTO reviewDTO = adminReviewService.moderateReview(reviewId, reviewStatus);
        return ResponseEntity.ok(reviewDTO);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        adminReviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<ProductReviewDTO>> getReviewsByStatus(
            @RequestParam String status,
            Pageable pageable) {
        ProductReview.ReviewStatus reviewStatus = ProductReview.ReviewStatus.valueOf(status.toUpperCase());
        Page<ProductReviewDTO> reviews = adminReviewService.getReviewsByStatus(reviewStatus, pageable);
        return ResponseEntity.ok(reviews);
    }
}