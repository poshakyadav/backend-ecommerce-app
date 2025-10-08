package com.ecommerce.bkd.records.exchangeOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ecommerce.bkd.models.enums.ExchangeOrderStatus;

public record ExchangeOrderDTO(
    Long id,
    Long orderId,
    Long orderItemId,
    Long replacementVariantId,
    ExchangeOrderStatus status,
    String reason,
    String description,
    String rejectReason,
    BigDecimal priceDifference,
    String paymentId, // Razorpay payment ID
    String payoutId,
    String shiprocketExchangeOrderId,
    String shiprocketExchangeShipmentId,
    String shiprocketExchangeAwbCode,
    LocalDateTime requestedAt,
    LocalDateTime approvedAt,
    LocalDateTime rejectedAt,
    LocalDateTime completedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String razorpayOrderId, // New field for Razorpay order ID
    BigDecimal paymentAmount, // New field for payment amount
    String currency, // New field for currency
    String replacementSize

) {}