package com.ecommerce.bkd.dtos.payment;

import lombok.Data;

@Data
public class PaymentVerificationRequest {
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    private String paymentMethod; // e.g. "card" or "upi", used if you need to save a PaymentMethod
    private Long orderId; // yourInternalOrderId
}
