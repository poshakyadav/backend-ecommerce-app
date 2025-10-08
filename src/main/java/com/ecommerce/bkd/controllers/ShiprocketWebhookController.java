package com.ecommerce.bkd.controllers;

import javax.crypto.Mac;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ecommerce.bkd.config.ShiprocketConfig;
import com.ecommerce.bkd.dtos.shiprocket.ShiprocketWebhookEvent;
import com.ecommerce.bkd.models.enums.ExchangeOrderStatus;
import com.ecommerce.bkd.repositories.ExchangeOrderRepository;
import com.ecommerce.bkd.services.impl.AdminExchangeService;
import com.ecommerce.bkd.services.impl.ReturnTrackingService;
import com.ecommerce.bkd.services.impl.ShiprocketTrackingService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/shipping/webhook")
@RequiredArgsConstructor
@Slf4j
public class ShiprocketWebhookController {

    private static final String HEADER_SIGNATURE = "X-Shiprocket-Signature";
    private static final String HEADER_APIKEY = "x-api-key";

    private final AdminExchangeService exchangeService;
    private final ExchangeOrderRepository exchangeRepo;
    private final ShiprocketTrackingService trackingService;
    private final ReturnTrackingService returnTrackingService;
    private final ShiprocketConfig config;
    private final ObjectMapper objectMapper; // for payload→event deserialization
    private final Mac hmacSha256; // pre-initialized bean

    /**
     * Receives Shiprocket tracking webhooks.
     * 
     * Required headers:
     * - x-api-key: your configured webhook secret
     * - X-Shiprocket-Signature: HMAC-SHA256 signature of the raw JSON body
     */
    @PostMapping
    public ResponseEntity<Void> handleWebhook(
            HttpServletRequest request,
            @RequestHeader(HEADER_APIKEY) String apiKey,
            @RequestHeader(HEADER_SIGNATURE) String signature,
            @RequestBody String payload) {

        // 1) IP Whitelisting
        String remoteIp = request.getRemoteAddr();
        if (!config.getWhitelistedIps().contains(remoteIp)) {
            log.warn("Rejected webhook from unauthorized IP: {}", remoteIp);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 2) API-key check (shared secret header)
        if (!config.getWebhookSecret().equals(apiKey)) {
            log.warn("Invalid Shiprocket webhook secret header: {}", apiKey);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 3) Signature verification (HMAC-SHA256)
        if (!verifySignature(payload, signature)) {
            log.warn("Invalid Shiprocket webhook signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 4) Deserialize and process
        try {
            ShiprocketWebhookEvent event = objectMapper.readValue(
                    payload, ShiprocketWebhookEvent.class);
            // trackingService.handleWebhookUpdate(event);
            if (event.isReturn()) {
                returnTrackingService.handleReturnEvent(event);
            } else {
                trackingService.handleWebhookUpdate(event);
            }
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            log.error("Failed to parse Shiprocket webhook payload", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/exchange")
    public void handleExchangeWebhook(@RequestBody Map<String, Object> payload) {
        String shipmentId = (String) payload.get("shipment_id");
        Integer statusCode = (Integer) payload.get("status_code");
        String awbCode = (String) payload.get("awb_code");

        exchangeRepo.findByShiprocketExchangeShipmentId(shipmentId).ifPresent(eo -> {
            switch (statusCode) {
                case 28: // Return Pickup Generated
                    eo.setShiprocketExchangeAwbCode(awbCode);
                    eo.setStatus(ExchangeOrderStatus.AWB_GENERATED);
                    break;
                case 34: // Out For Pickup
                    eo.setStatus(ExchangeOrderStatus.PICKUP_SCHEDULED);
                    break;
                case 32: // Returned Picked Up
                    eo.setStatus(ExchangeOrderStatus.PICKED_UP);
                    break;
                case 25: // Return In Transit
                    eo.setStatus(ExchangeOrderStatus.IN_TRANSIT);
                    break;
                case 26: // Return Delivered
                    exchangeService.processExchangeCompletion(eo);
                    break;
                case 27: // Return Cancelled
                    eo.setStatus(ExchangeOrderStatus.CANCELLED);
                    exchangeService.cancelExchange(eo.getId(), eo.getOrder().getUser().getId());
                    break;
                case 24: // Return Pickup Error
                case 57: // QC Failed
                    eo.setStatus(ExchangeOrderStatus.FAILED);
                    eo.setRejectReason("Shiprocket error: " + payload.get("status"));
                    break;
                default:
                    break;
            }
            exchangeRepo.save(eo);
        });
    }

    private boolean verifySignature(String payload, String signature) {
        try {
            byte[] computed = hmacSha256.doFinal(
                    payload.getBytes(StandardCharsets.UTF_8));
            String expected = Base64.getEncoder().encodeToString(computed);
            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Error verifying Shiprocket signature", e);
            return false;
        }
    }
}
