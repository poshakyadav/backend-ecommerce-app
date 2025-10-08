package com.ecommerce.bkd.repositories;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.bkd.models.RpWebhookEvent;

public interface RpWebhookEventRepository extends JpaRepository<RpWebhookEvent, String> {

    /**
     * Delete all webhook‐event records processed before the given cutoff.
     */
    void deleteByProcessedAtBefore(LocalDateTime cutoff);

}
