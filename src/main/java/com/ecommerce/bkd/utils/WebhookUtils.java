package com.ecommerce.bkd.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import com.ecommerce.bkd.dtos.shiprocket.ShiprocketWebhookEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookUtils {

    @Value("${shiprocket.webhook.secret}")
    private String webhookSecret;

    private final ObjectMapper objectMapper;

    public boolean isValidShiprocketSignature(String payload, String signature) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(webhookSecret.getBytes(), "HmacSHA256");
            sha256Hmac.init(secretKey);
            String calculatedSignature = Base64.getEncoder()
                    .encodeToString(sha256Hmac.doFinal(payload.getBytes()));
            return signature.equals(calculatedSignature);
        } catch (Exception e) {
            log.error("Error validating webhook signature: ", e);
            return false;
        }
    }

    public ShiprocketWebhookEvent parseWebhookPayload(String payload) {
        try {
            return objectMapper.readValue(payload, ShiprocketWebhookEvent.class);
        } catch (Exception e) {
            log.error("Error parsing webhook payload: ", e);
            throw new IllegalArgumentException("Invalid webhook payload");
        }
    }
}
