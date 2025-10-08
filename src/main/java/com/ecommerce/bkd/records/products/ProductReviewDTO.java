package com.ecommerce.bkd.records.products;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewDTO {
    private Long id;
    private Long userId;
    private String username;
    private Integer rating;
    private String reviewText;
    private List<String> imageUrls;
    private String status;
    private Long helpfulVotes;
    private Long unhelpfulVotes;
    private LocalDateTime createdAt;
    private boolean verifiedPurchase;
}