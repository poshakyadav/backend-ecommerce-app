# Caching with Caffeine

## Introduction
Caffeine is a high-performance Java caching library. In our e-commerce application, we use it to cache frequently accessed data like pincode serviceability checks.

## Basic Concepts

### Cache Terminology
- **Cache Hit**: Data found in cache
- **Cache Miss**: Data not found, needs to be loaded
- **Eviction**: Removing entries from cache
- **TTL (Time To Live)**: How long entries stay valid

## Implementation

### 1. Basic Configuration
```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public Caffeine caffeineConfig() {
        return Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(1000)
            .recordStats();
    }
}
```

### 2. Usage Examples

Simple Caching:
```java
@Cacheable(value = "serviceabilityCheck", key = "#pincode")
public ServiceabilityCheckResponse checkServiceability(String pincode) {
    // This will be cached
    return serviceableAreaRepository.findByPincode(pincode)
        .map(/* ... */);
}
```

Cache Eviction:
```java
@CacheEvict(value = "serviceabilityCheck", key = "#area.pincode")
public ServiceableArea updateServiceableArea(ServiceableArea area) {
    // Cache will be cleared for this pincode
}
```

## Advanced Features

### 1. Cache Policies

Size-based:
```java
Caffeine.newBuilder()
    .maximumSize(10_000)
```

Time-based:
```java
Caffeine.newBuilder()
    .expireAfterWrite(5, TimeUnit.MINUTES)
    .expireAfterAccess(10, TimeUnit.MINUTES)
```

### 2. Cache Loading
```java
LoadingCache<String, ServiceabilityCheckResponse> cache = Caffeine.newBuilder()
    .build(pincode -> checkServiceability(pincode));
```

### 3. Cache Statistics
```java
CacheStats stats = cache.stats();
log.info("Hit rate: {}", stats.hitRate());
log.info("Miss rate: {}", stats.missRate());
log.info("Load success count: {}", stats.loadSuccessCount());
```

## Monitoring

### Real-time Monitoring
```java
@Scheduled(fixedRate = 300000) // Every 5 minutes
public void monitorCache() {
    cacheManager.getCacheNames().forEach(cacheName -> {
        CaffeineCache cache = (CaffeineCache) cacheManager.getCache(cacheName);
        CacheStats stats = cache.getNativeCache().stats();
        // Log statistics
    });
}
```

## Cache Strategies

### 1. What to Cache
- Frequently accessed data
- Data that's expensive to compute
- Relatively static data

Example:
```java
// Good candidate for caching
@Cacheable("productDetails")
public ProductDTO getProductDetails(Long id) {
    // Complex aggregation
}

// Bad candidate for caching
@Cacheable("userCart")
public CartDTO getUserCart(Long userId) {
    // Frequently changing data
}
```

### 2. Cache Invalidation Strategies
1. Time-based (TTL)
2. Event-based (explicit eviction)
3. Size-based (automatic eviction)

## Best Practices
1. Set appropriate cache sizes
2. Monitor cache hit rates
3. Use meaningful cache names
4. Implement cache warming
5. Handle cache failures gracefully
