package com.ecommerce.bkd.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpStatusCode;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import java.time.Duration;
import java.util.function.Supplier;
import org.springframework.core.ParameterizedTypeReference;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ecommerce.bkd.exceptions.ShiprocketApiException;
import com.ecommerce.bkd.services.ShiprocketAuthService;
import com.google.common.util.concurrent.RateLimiter;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiprocketApiService {

    private final WebClient shiprocketWebClient;
    private final ShiprocketAuthService authService;
    private final RateLimiter rateLimiter;

    public <T> T get(String path, Class<T> responseType) {
        return executeWithRetry(() -> shiprocketWebClient
                .get().uri(path)
                .header("Authorization", "Bearer " + authService.getAuthToken())
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapToApiError) // <-- fixed here
                .bodyToMono(responseType));
    }

    public <T> T post(String path, Object request, Class<T> responseType) {
        return executeWithRetry(() -> shiprocketWebClient
                .post().uri(path)
                .header("Authorization", "Bearer " + authService.getAuthToken())
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapToApiError) // <-- and here
                .bodyToMono(responseType));
    }

    public <T> T get(String path, ParameterizedTypeReference<T> typeRef) {
        return executeWithRetry(() -> shiprocketWebClient
                .get().uri(path)
                .header("Authorization", "Bearer " + authService.getAuthToken())
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapToApiError)
                .bodyToMono(typeRef));
    }

    public <T> T post(String path, Object request, ParameterizedTypeReference<T> typeRef) {
        return executeWithRetry(() -> shiprocketWebClient
                .post().uri(path)
                .header("Authorization", "Bearer " + authService.getAuthToken())
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapToApiError)
                .bodyToMono(typeRef));
    }

    private <T> T executeWithRetry(Supplier<Mono<T>> operation) {
        rateLimiter.acquire();
        return operation.get()
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(this::isRetryableException))
                .doOnError(e -> log.error("Shiprocket API failure", e))
                .block();
    }

    private boolean isRetryableException(Throwable e) {
        return !(e instanceof ShiprocketApiException);
    }

    private Mono<? extends Throwable> mapToApiError(ClientResponse resp) {
        return resp.bodyToMono(String.class)
                .flatMap(body -> {
                    HttpStatusCode code = resp.statusCode();
                    if (code.value() == 429) {
                        return Mono.error(new ShiprocketApiException("Rate limit exceeded"));
                    }
                    if (code.value() == 422) {
                        return Mono.error(new ShiprocketApiException("Validation failed: " + body));
                    }
                    return Mono.error(new ShiprocketApiException(
                            "API error " + code.value() + ": " + body));
                });
    }
}
