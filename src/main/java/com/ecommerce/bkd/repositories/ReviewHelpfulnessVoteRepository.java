package com.ecommerce.bkd.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.bkd.models.ReviewHelpfulnessVote;

public interface ReviewHelpfulnessVoteRepository extends JpaRepository<ReviewHelpfulnessVote, Long> {
    Optional<ReviewHelpfulnessVote> findByReviewIdAndUserId(Long reviewId, Long userId);
}