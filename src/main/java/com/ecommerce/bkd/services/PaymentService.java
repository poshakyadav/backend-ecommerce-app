package com.ecommerce.bkd.services;

import com.ecommerce.bkd.dtos.payment.PaymentDTO;
import com.ecommerce.bkd.dtos.payment.PaymentInitiateRequest;
import com.ecommerce.bkd.dtos.payment.PaymentVerificationRequest;
import com.ecommerce.bkd.models.ExchangeOrder;
import com.ecommerce.bkd.models.Payment;
import com.ecommerce.bkd.models.ReturnOrder;
import com.ecommerce.bkd.models.enums.PaymentStatus;
import com.razorpay.RazorpayException;


import org.json.JSONObject;

public interface PaymentService {

    JSONObject initiatePayment(PaymentInitiateRequest request) throws RazorpayException;

    JSONObject initiateExchangePayment(PaymentInitiateRequest request) throws RazorpayException;

    Payment refundPayment(Long orderId) throws RazorpayException;

    boolean verifyPayment(PaymentVerificationRequest request) throws RazorpayException;

    PaymentStatus checkPaymentStatus(Long paymentId);


    PaymentDTO getPaymentDetails(String paymentId) throws RazorpayException;

    void capturePayment(String paymentId) throws RazorpayException;

    void initiateManualRefund(ReturnOrder returnOrder) throws RazorpayException;

    void initiateExchangeRefund(ExchangeOrder eo);

    String getSecretKey();
}
