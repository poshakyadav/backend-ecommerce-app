package com.ecommerce.bkd.controllers;

import java.time.LocalDateTime;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.bkd.exceptions.ResourceNotFoundException;
import com.ecommerce.bkd.models.ExchangeOrder;
import com.ecommerce.bkd.models.Order;
import com.ecommerce.bkd.models.OrderPaymentStatusHistory;
import com.ecommerce.bkd.models.Payment;
import com.ecommerce.bkd.models.ReturnOrder;
import com.ecommerce.bkd.models.enums.ExchangeOrderStatus;
import com.ecommerce.bkd.models.enums.OrderPaymentStatus;
import com.ecommerce.bkd.models.enums.PaymentStatus;
import com.ecommerce.bkd.repositories.ExchangeOrderRepository;
import com.ecommerce.bkd.repositories.OrderRepository;
import com.ecommerce.bkd.repositories.PaymentRepository;
import com.ecommerce.bkd.repositories.ReturnOrderRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/razorpay/x/webhook")
@RequiredArgsConstructor
public class RazorpayPayoutWebhookController {

    private final PaymentRepository paymentRepository;
    private final ReturnOrderRepository returnOrderRepository;
    private final OrderRepository orderRepository;
    private final ExchangeOrderRepository exchangeOrderRepository;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @Transactional
    @PostMapping
    public ResponseEntity<Void> handlePayoutWebhook(
        @RequestHeader("X-Razorpay-Signature") String signature,
        @RequestHeader(value = "X-Razorpay-Event-Id", required = false) String eventId,
        @RequestBody String payload) {
        try {
            // 1) Verify signature
            com.razorpay.Utils.verifyWebhookSignature(payload, signature, webhookSecret);

            JSONObject json = new JSONObject(payload);
            String event = json.getString("event");
            JSONObject payoutEntity = json.getJSONObject("payload")
                                       .getJSONObject("payout")
                                       .getJSONObject("entity");
            String payoutId = payoutEntity.getString("id");
            String status = payoutEntity.getString("status");

            // 2) Fetch associated Payment
            Payment payment = paymentRepository.findByTransactionId(payoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for payout " + payoutId));
            
            // 3) Update based on event/status
            switch (event) {
                case "payout.initiated", "payout.pending", "payout.queued" -> {
                    payment.setStatus(PaymentStatus.REFUND_INITIATED);
                }
                case "payout.processed" -> {
                    payment.setStatus(PaymentStatus.REFUNDED);
                    
                   if (payment.getReturnOrderId() != null) {
                        ReturnOrder ro = returnOrderRepository.findById(payment.getReturnOrderId())
                            .orElseThrow();
                        ro.setPaymentStatus(OrderPaymentStatus.REFUNDED);
                        ro.setRefundedAt(LocalDateTime.now());
                        returnOrderRepository.save(ro);

                        Order order = ro.getOrder();
                        order.setOrderPaymentStatus(OrderPaymentStatus.REFUNDED);
                        order.getOrderPaymentStatusHistory().add(
                            new OrderPaymentStatusHistory(order, OrderPaymentStatus.REFUNDED,
                                "Refund processed, payoutId=" + payoutId, LocalDateTime.now())
                        );
                        orderRepository.save(order);
                    } else if (payment.getExchangeOrderId() != null) {
                        ExchangeOrder eo = exchangeOrderRepository.findById(payment.getExchangeOrderId())
                            .orElseThrow();
                        eo.setStatus(ExchangeOrderStatus.EXCHANGE_REFUND_COMPLETED);
                        exchangeOrderRepository.save(eo);

                        Order order = eo.getOrder();
                        order.getOrderPaymentStatusHistory().add(
                            new OrderPaymentStatusHistory(order, OrderPaymentStatus.REFUNDED,
                                "Exchange refund processed, payoutId=" + payoutId, LocalDateTime.now())
                        );
                        orderRepository.save(order);
                    }
                    break;
                }
                case "payout.failed", "payout.rejected", "payout.reversed" -> {
                    if (payment.getReturnOrderId() != null) {
                        ReturnOrder ro = returnOrderRepository.findById(payment.getReturnOrderId())
                            .orElseThrow();
                        ro.setPaymentStatus(OrderPaymentStatus.REFUND_FAILED);
                        returnOrderRepository.save(ro);

                        Order order = ro.getOrder();
                        order.setOrderPaymentStatus(OrderPaymentStatus.REFUND_FAILED);
                        order.getOrderPaymentStatusHistory().add(
                            new OrderPaymentStatusHistory(order, OrderPaymentStatus.REFUND_FAILED,
                                "Refund failed for payoutId=" + payoutId, LocalDateTime.now())
                        );
                        orderRepository.save(order);
                    } else if (payment.getExchangeOrderId() != null) {
                        ExchangeOrder eo = exchangeOrderRepository.findById(payment.getExchangeOrderId())
                            .orElseThrow();
                        eo.setStatus(ExchangeOrderStatus.EXCHANGE_REFUND_FAILED);
                        exchangeOrderRepository.save(eo);

                        Order order = eo.getOrder();
                        order.setOrderPaymentStatus(OrderPaymentStatus.REFUND_FAILED);
                        order.getOrderPaymentStatusHistory().add(
                            new OrderPaymentStatusHistory(order, OrderPaymentStatus.REFUND_FAILED,
                                "Exchange refund failed for payoutId=" + payoutId, LocalDateTime.now())
                        );
                        orderRepository.save(order);
                    }
                    break;
                }
                default -> log.info("Unhandled payout event/status: {} / {}", event, status);
            }

            paymentRepository.save(payment);
            return ResponseEntity.ok().build();

        } catch (com.razorpay.RazorpayException e) {
            log.warn("Invalid webhook signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error handling RazorpayX payout webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
