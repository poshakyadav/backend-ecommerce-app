package com.ecommerce.bkd.models.enums;

public enum OrderPaymentStatus {

    PAYMENT_PENDING,  
    PAYMENT_SUCCESS,
    PAYMENT_FAILED, 

    REFUND_PENDING,
    // RETURN_REFUND_PENDING,
    REFUND_INITIATED, // Refund process started
    EXCHANGE_REFUND_INITIATED, // Refund process started for exchange
    REFUND_FAILED, // Refund process failed
    REFUNDED, // Refund completed

    /**
     * This NO_REFUND_REQUIRED status indicates that when user is cancel the order and if user does not pay any amount for order like COD then on cancel order does not require a refund.
     */
    NO_REFUND_REQUIRED

}
