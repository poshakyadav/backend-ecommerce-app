package com.ecommerce.bkd.services.impl;

import org.json.JSONObject;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.ecommerce.bkd.dtos.payment.PaymentInitiateRequest;
import com.ecommerce.bkd.services.PaymentService;
import com.razorpay.RazorpayException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentRetryService {

    private final PaymentService paymentService;

    @Retryable(maxAttempts = 3)
    public JSONObject retryPaymentInitiation(PaymentInitiateRequest request) throws RazorpayException {
        return paymentService.initiatePayment(request);
    }

    // ... other retry methods ...
}
