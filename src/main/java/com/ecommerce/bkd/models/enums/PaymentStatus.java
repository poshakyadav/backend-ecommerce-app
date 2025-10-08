package com.ecommerce.bkd.models.enums;

public enum PaymentStatus {
    PENDING,
    AUTHORIZED,
    COMPLETED,
    FAILED,
    REFUND_PENDING,
    REFUND_INITIATED, // Refund process started
    EXCHANGE_REFUND_INITIATED, // Refund process started for exchange
    REFUNDED, // Refund completed
    REFUND_FAILED, // Refund process failed
    UNKNOWN
}
