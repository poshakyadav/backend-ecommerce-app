package com.ecommerce.bkd.schedulars;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ecommerce.bkd.repositories.RpWebhookEventRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CleanupOldRpWebhookEvents {

    private final RpWebhookEventRepository webhookEventRepository;

    // Runs every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupOldWebhookEvents() {
        webhookEventRepository.deleteByProcessedAtBefore(
                LocalDateTime.now().minusDays(7));
    }

}
