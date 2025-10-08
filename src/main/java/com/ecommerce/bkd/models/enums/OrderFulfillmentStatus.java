package com.ecommerce.bkd.models.enums;

public enum OrderFulfillmentStatus {

    CANCELLED, // Order was cancelled (User/Admin can cancel)

    CREATED, // Order created (User Role)
    SHIPROCKET_ORDER_CREATION_FAILED,
    PROCESSING, // Order is being processed (Admin Role)
    ACCEPTED, // Order accepted by the seller (Admin Role)
    SERVICEABILITY_CHECK_FAILED,
    COURIER_SELECTION_IN_PROGRESS, // Courier selection in progress (Admin Role)
    COURIER_SELECTED, // Courier selected (Admin Role)
    AWB_GENERATED, // AWB generated (Admin Role)
    SR_AWB_GENERATION_FAILED,
    READY_TO_SHIP, // Order is ready to ship (Admin Role)
    SR_PICKUP_REQUEST_FAILED,

    SHIPPED, // Order has been shipped (Admin Role), track this status by calling webhook
    SHIPMENT_FAILED, // Shipment failed (e.g., courier unable to pick up)
    IN_TRANSIT, // Order is in transit , track this status by calling webhook
    OUT_FOR_DELIVERY, // Order is out for delivery, track this status by calling webhook
    DELIVERED, // Order successfully delivered, track this status by calling webhook
    UNDELIVERED, // Order not delivered (e.g., customer not available) (If delivery fails, might

    RTO_INITIATED, // RTO process initiated (e.g., return to origin)
    RTO_DELIVERED, // RTO delivered to the seller
    RTO_REJECTED, // RTO rejected by the seller

}
