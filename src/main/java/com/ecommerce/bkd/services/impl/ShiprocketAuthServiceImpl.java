package com.ecommerce.bkd.services.impl;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.ecommerce.bkd.config.ShiprocketConfig;
import com.ecommerce.bkd.dtos.shiprocket.ShiprocketAuthResponse;
import com.ecommerce.bkd.exceptions.ShiprocketApiException;
import com.ecommerce.bkd.services.ShiprocketAuthService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiprocketAuthServiceImpl implements ShiprocketAuthService {

    private final WebClient shiprocketWebClient;
    private final ShiprocketConfig config;

    @Override
    @Cacheable(value = "shiprocketToken")
    public String getAuthToken() {
        try {
            ShiprocketAuthResponse response = shiprocketWebClient
                    .post()
                    .uri("/auth/login")
                    .bodyValue(createLoginRequest())
                    .retrieve()
                    .bodyToMono(ShiprocketAuthResponse.class)
                    .block();

            if (response != null && response.getToken() != null) {
                return response.getToken();
            }
            throw new ShiprocketApiException("Failed to get auth token");
        } catch (Exception e) {
            log.error("Error getting Shiprocket auth token: ", e);
            throw new ShiprocketApiException("Authentication failed: " + e.getMessage());
        }
    }

    public void logout() {
        try {
            shiprocketWebClient.post()
                    .uri("/auth/logout")
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            log.error("Error during logout: ", e);
            throw new ShiprocketApiException("Logout failed: " + e.getMessage());
        }
    }

    private LoginRequest createLoginRequest() {
        return new LoginRequest(config.getApiEmail(), config.getApiPassword());
    }

    @Data
    @AllArgsConstructor
    private static class LoginRequest {
        private String email;
        private String password;
    }
}
