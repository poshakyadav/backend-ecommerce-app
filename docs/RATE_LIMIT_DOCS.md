# Rate Limiting with Bucket4j

## Introduction
Rate limiting is a technique used to control the rate of requests a user can make to an API. In our e-commerce application, we use Bucket4j to implement a token bucket algorithm for rate limiting.

## Basic Concepts

### Token Bucket Algorithm
- Think of it as a bucket that holds tokens
- Each API request consumes one token
- Tokens are refilled at a fixed rate
- If bucket is empty, requests are rejected

Example:
```java
Bucket bucket = Bucket.builder()
    .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))))
    .build();
```
This creates a bucket that:
- Holds maximum 100 tokens
- Refills 100 tokens every minute
- Each request needs 1 token

## Implementation Details

### 1. Configuration
```java
@Configuration
public class RateLimitingConfig {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    // Each IP gets its own bucket
    public Bucket resolveBucket(String ipAddress) {
        return buckets.computeIfAbsent(ipAddress, this::newBucket);
    }
}
```

### 2. Usage Examples

Basic Usage:
```java
// Allow 100 requests per minute
if (bucket.tryConsume(1)) {
    // Process request
} else {
    // Return 429 Too Many Requests
}
```

Different Limits:
```java
// For public API
Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));

// For admin API
Bandwidth adminLimit = Bandwidth.classic(1000, Refill.intervally(1000, Duration.ofMinutes(1)));
```

## Advanced Features

### 1. Multiple Limits
```java
Bucket.builder()
    .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofSeconds(1)))) // per second
    .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)))) // per minute
    .build();
```

### 2. Conditional Limits
```java
if (user.isPremium()) {
    return Bucket.builder()
        .addLimit(premiumLimit)
        .build();
} else {
    return Bucket.builder()
        .addLimit(basicLimit)
        .build();
}
```

## Monitoring and Troubleshooting

### View Active Buckets
```java
public Map<String, Long> getRemainingTokens() {
    return buckets.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> e.getValue().getAvailableTokens()
        ));
}
```

## Best Practices
1. Set appropriate limits based on:
   - Server capacity
   - Business requirements
   - User types
2. Monitor rate limit hits
3. Provide clear error messages
4. Consider implementing retry mechanisms

---
---

## Explaining how rate limitting play his role on this application

Rate limiting in this backend ecommerce application is implemented using the **Bucket4j** library, which follows the **token bucket algorithm**. Here's how it works step by step:

---

### 1. **Configuration: `RateLimitingConfig`**
The `RateLimitingConfig` class is responsible for creating and managing rate-limiting buckets for each client. 

- **Key Points:**
  - A `ConcurrentHashMap` is used to store buckets, where each bucket is associated with a unique key (e.g., the client's IP address).
  - The `newBucket` method creates a bucket with a limit of **100 requests per minute** (`Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)))`).
  - The `resolveBucket` method ensures that each client gets their own bucket, creating one if it doesn't already exist.

---

### 2. **Interceptor: `RateLimitInterceptor`**
The `RateLimitInterceptor` enforces the rate-limiting rules for incoming requests.

- **Key Points:**
  - The `preHandle` method is called before a request is processed.
  - It identifies the client using their IP address (`request.getRemoteAddr()`).
  - The bucket for the client is retrieved using `RateLimitingConfig`.
  - If the bucket has available tokens (`bucket.tryConsume(1)`), the request is allowed to proceed.
  - If no tokens are available, the request is rejected with a **429 Too Many Requests** response, along with a JSON error message.

---

### 3. **Integration: `WebMvcConfig`**
The `WebMvcConfig` class integrates the `RateLimitInterceptor` into the Spring MVC framework.

- **Key Points:**
  - The `addInterceptors` method registers the `RateLimitInterceptor` to intercept all requests matching the `/api/**` pattern.
  - This ensures that rate limiting is applied only to API endpoints.

---

### **How It Works Together**
1. When a client sends a request to an API endpoint (e.g., `/api/products`), the `RateLimitInterceptor` is triggered.
2. The interceptor checks the client's IP address and retrieves their corresponding bucket.
3. If the bucket has tokens available, the request is processed normally.
4. If the bucket is empty, the client receives a **429 Too Many Requests** response, preventing further requests until tokens are refilled.

---

### **Why This is Effective**
- **Fairness:** Each client is rate-limited independently based on their IP address.
- **Scalability:** The use of `Bucket4j` ensures efficient token management with minimal overhead.
- **Customizability:** You can easily adjust the rate limits or add more complex rules (e.g., different limits for different user roles).