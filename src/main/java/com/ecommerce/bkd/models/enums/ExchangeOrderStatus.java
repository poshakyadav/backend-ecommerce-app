package com.ecommerce.bkd.models.enums;

public enum ExchangeOrderStatus {
    PENDING_PAYMENT, // Status for awaiting payment
    REQUESTED,          // Initial request (Shiprocket: 21 - Return Pending)
    APPROVED,          // Admin approved
    REJECTED,          // Admin rejected
    INITIATED,         // Shiprocket exchange order created
    AWB_PENDING,       // Waiting for AWB (Shiprocket: 23 - Return Pickup Queued)
    AWB_GENERATED,     // AWB assigned (Shiprocket: 28 - Return Pickup Generated)
    PICKUP_SCHEDULED,  // Pickup scheduled (Shiprocket: 34 - Out For Pickup)
    PICKED_UP,         // Item picked up (Shiprocket: 32 - Returned Picked Up)
    IN_TRANSIT,        // Return in transit (Shiprocket: 25 - Return In Transit)
    COMPLETED,         // Exchange completed (Shiprocket: 26 - Return Delivered)
    FAILED,            // Exchange failed (Shiprocket: 24 - Return Pickup Error, etc.)
    CANCELLED,          // Exchange cancelled (Shiprocket: 27 - Return Cancelled)

    // Statuses for exchange refund process
    EXCHANGE_REFUND_PENDING,   // Refund pending for exchange
    EXCHANGE_REFUND_INITIATED, // Refund process started for exchange
    EXCHANGE_REFUND_FAILED,    // Refund process failed for exchange
    EXCHANGE_REFUND_COMPLETED, // Refund completed for exchange
}