package com.ecommerce.bkd.schedulars;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.bkd.repositories.CartRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CartCleanupTask {

    private final CartRepository cartRepository;

    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    @Transactional
    public void cleanupOldGuestCarts() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        cartRepository.deleteByUserIsNullAndLastUpdatedBefore(threshold);
    }
}