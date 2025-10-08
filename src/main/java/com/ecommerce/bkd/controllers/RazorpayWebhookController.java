package com.ecommerce.bkd.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.bkd.models.ExchangeOrder;
import com.ecommerce.bkd.models.Order;
import com.ecommerce.bkd.models.RpWebhookEvent;
import com.ecommerce.bkd.models.enums.ExchangeOrderStatus;
import com.ecommerce.bkd.models.enums.OrderPaymentStatus;
import com.ecommerce.bkd.repositories.ExchangeOrderRepository;
import com.ecommerce.bkd.repositories.OrderRepository;
import com.ecommerce.bkd.repositories.RpWebhookEventRepository;
import com.ecommerce.bkd.services.impl.UserExchangeService;
import com.ecommerce.bkd.services.impl.UserOrderService;
import com.ecommerce.bkd.utils.PaymentUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/razorpay/webhook")
@RequiredArgsConstructor
public class RazorpayWebhookController {

    private final OrderRepository orderRepository;
    private final ExchangeOrderRepository exchangeOrderRepository;
    private final RpWebhookEventRepository webhookEventRepository;
    private final UserOrderService userOrderService;
    private final UserExchangeService userExchangeService;

    @Value("${razorpay.webhook.secret}")
    private String razorpayWebhookSecret;

    @Transactional
    @PostMapping
    public ResponseEntity<?> handleWebhook(
        @RequestHeader("X-Razorpay-Signature") String signature,
        @RequestHeader("X-Razorpay-Event-Id") String eventId,
        @RequestBody String payload) {
        if (webhookEventRepository.existsById(eventId)) {
            return ResponseEntity.ok().build();
        }

        try {
            com.razorpay.Utils.verifyWebhookSignature(payload, signature, razorpayWebhookSecret);
            JSONObject json = new JSONObject(payload);
            String event = json.getString("event");

            switch (event) {
                case "payment.captured":
                    handlePaymentSuccess(json);
                    break;
                case "payment.failed":
                    handlePaymentFailure(json);
                    break;
                case "refund.processed":
                    handleRefundSuccess(json);
                    break;
                default:
                    log.info("Unhandled event: {}", event);
                    break;
            }
            markEventAsProcessed(eventId);
            return ResponseEntity.ok().build();
        } catch (com.razorpay.RazorpayException e) {
            log.warn("Invalid Razorpay webhook signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error processing Razorpay webhook: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void markEventAsProcessed(String eventId) {
        RpWebhookEvent evt = new RpWebhookEvent();
        evt.setEventId(eventId);
        evt.setProcessedAt(LocalDateTime.now());
        webhookEventRepository.save(evt);
    }

    private void handlePaymentSuccess(JSONObject webhookData) {
        try {
            JSONObject payment = webhookData.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");
            String orderId = payment.getJSONObject("notes").getString("order_id");
            String paymentId = payment.getString("id");
            String paymentType = payment.getJSONObject("notes").optString("type", "");

            if ("exchange".equals(paymentType)) {
                // Handle exchange order payment
                List<ExchangeOrder> exchangeOrders = exchangeOrderRepository.findByRazorpayOrderId(payment.getString("order_id"));
                if (exchangeOrders.isEmpty()) {
                    throw new IllegalStateException("No exchange order found for Razorpay order ID: " + payment.getString("order_id"));
                }
                for (ExchangeOrder eo : exchangeOrders) {
                    userExchangeService.processExchangePaymentSuccess(eo, paymentId);
                }
                log.info("✅ payment.captured handled for exchange order(s), paymentId={}", paymentId);
            } else {
                // Handle regular order payment
                Order order = orderRepository.findById(Long.parseLong(orderId))
                    .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));
                userOrderService.processPaymentSuccess(order, paymentId);
                log.info("✅ payment.captured handled for order {}", orderId);
            }
        } catch (Exception e) {
            log.error("Error in webhook handlePaymentSuccess: ", e);
        }
    }

    private void handlePaymentFailure(JSONObject webhookData) {
        try {
            JSONObject payment = webhookData.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");
            String orderId = payment.getJSONObject("notes").getString("order_id");
            String errorCode = payment.getString("error_code");
            String errorDescription = payment.getString("error_description");
            String paymentType = payment.getJSONObject("notes").optString("type", "");

            if ("exchange".equals(paymentType)) {
                List<ExchangeOrder> exchangeOrders = exchangeOrderRepository.findByRazorpayOrderId(payment.getString("order_id"));
                for (ExchangeOrder eo : exchangeOrders) {
                    eo.setStatus(ExchangeOrderStatus.FAILED);
                    eo.setRejectReason("Payment failed: " + errorDescription);
                    exchangeOrderRepository.save(eo);
                }
                log.error("❌ payment.failed handled for exchange order(s), error: {} - {}", errorCode, errorDescription);
            } else {
                Order order = orderRepository.findById(Long.parseLong(orderId))
                    .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));
                order.setOrderPaymentStatus(OrderPaymentStatus.PAYMENT_FAILED);
                order.setPaymentStatus("FAILED");
                orderRepository.save(order);
                log.error("❌ payment.failed handled for order: {}. Error: {} - {}", orderId, errorCode, errorDescription);
            }
        } catch (Exception e) {
            log.error("Error in handlePaymentFailure: ", e);
        }
    }

    private void handleRefundSuccess(JSONObject webhookData) {
        try {
            JSONObject refund = webhookData.getJSONObject("payload")
                .getJSONObject("refund")
                .getJSONObject("entity");
            String paymentId = refund.getString("payment_id");
            String refundId = refund.getString("id");

            Order order = orderRepository.findByRazorpayPaymentId(paymentId)
                .orElseThrow(() -> new IllegalStateException("Order not found for payment " + paymentId));
            order.setOrderPaymentStatus(OrderPaymentStatus.REFUNDED);
            order.setPaymentStatus("REFUNDED");
            orderRepository.save(order);

            log.info("💸 refund.processed handled for order {}, refundId={}", order.getId(), refundId);
        } catch (Exception e) {
            log.error("Error in handleRefundSuccess: ", e);
        }
    }
}