package com.ecommerce.bkd.services;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheMonitoringService {
    
    private final CacheManager cacheManager;

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void monitorCache() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache(cacheName);
            if (caffeineCache != null) {
                CacheStats stats = caffeineCache.getNativeCache().stats();
                log.info("Cache '{}' statistics:", cacheName);
                log.info("Hit ratio: {}", stats.hitRate());
                log.info("Miss ratio: {}", stats.missRate());
                log.info("Load success count: {}", stats.loadSuccessCount());
                log.info("Load failure count: {}", stats.loadFailureCount());
                log.info("Total load time: {}", stats.totalLoadTime());
                log.info("Eviction count: {}", stats.evictionCount());
            }
        });
    }

    public void clearCache(String cacheName) {
        CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache(cacheName);
        if (caffeineCache != null) {
            caffeineCache.clear();
            log.info("Cache '{}' cleared", cacheName);
        }
    }
}
