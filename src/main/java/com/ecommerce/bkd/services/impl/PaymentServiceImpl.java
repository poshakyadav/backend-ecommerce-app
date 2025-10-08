package com.ecommerce.bkd.services.impl;

import com.ecommerce.bkd.dtos.payment.PaymentDTO;
import com.ecommerce.bkd.dtos.payment.PaymentInitiateRequest;
import com.ecommerce.bkd.dtos.payment.PaymentVerificationRequest;
import com.ecommerce.bkd.models.ExchangeOrder;
import com.ecommerce.bkd.models.ExchangePaymentDetails;
import com.ecommerce.bkd.models.Order;
import com.ecommerce.bkd.models.OrderPaymentStatusHistory;
import com.ecommerce.bkd.models.Payment;
import com.ecommerce.bkd.models.enums.ExchangeOrderStatus;
import com.ecommerce.bkd.models.enums.OrderPaymentStatus;
import com.ecommerce.bkd.models.enums.PaymentStatus;
import com.ecommerce.bkd.models.RazorpayPayment;
import com.ecommerce.bkd.models.ReturnOrder;
import com.ecommerce.bkd.models.ReturnPaymentDetails;
import com.ecommerce.bkd.models.User;
import com.ecommerce.bkd.repositories.ExchangeOrderRepository;
import com.ecommerce.bkd.repositories.OrderRepository;
import com.ecommerce.bkd.repositories.PaymentRepository;
import com.ecommerce.bkd.repositories.RazorpayPaymentRepository;
import com.ecommerce.bkd.repositories.ReturnOrderRepository;
import com.ecommerce.bkd.repositories.UserRepository;
import com.ecommerce.bkd.services.PaymentService;
import com.ecommerce.bkd.utils.PaymentUtils;
import com.ecommerce.bkd.exceptions.PaymentProcessingException;
import com.ecommerce.bkd.exceptions.ResourceNotFoundException;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import jakarta.transaction.Transactional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final RazorpayClient razorpayClient; // Injected from RazorpayConfig
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ReturnOrderRepository returnOrderRepository;
    private final RazorpayPaymentRepository razorpayPaymentRepository;
    private final RazorpayXPayoutService xPayoutService;
    private final UserRepository userRepository;
    private final ExchangeOrderRepository exchangeOrderRepository;

    @Value("${razorpay.key.secret}")
    private String secretKey;

    @Override
    @Transactional
    public JSONObject initiatePayment(PaymentInitiateRequest request) throws RazorpayException {
        try {
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

            // Validate order status
            if (order.getOrderPaymentStatus() != OrderPaymentStatus.PAYMENT_PENDING) {
                throw new IllegalStateException("Order is not in PAYMENT_PENDING state");
            }

            // Must match the order’s final payable amount
            BigDecimal expected = Optional.ofNullable(order.getTotalOrderPrice()).orElse(BigDecimal.ZERO);
            if (expected.compareTo(request.getAmount()) != 0) {
                throw new IllegalArgumentException("Payment amount does not match order total");
            }

            JSONObject options = new JSONObject()
                    .put("amount", expected.movePointRight(2).intValueExact()) // paise
                    .put("currency", request.getCurrency())
                    .put("receipt", order.getOrderNumber())
                    .put("notes", new JSONObject()
                            .put("order_id", order.getId().toString())
                            .put("customer_email", order.getUser().getEmail()));

            com.razorpay.Order razorpayOrder = razorpayClient.orders.create(options);

            // Persist Razorpay order record
            RazorpayPayment rzp = new RazorpayPayment();
            rzp.setOrder(order);
            rzp.setRazorpayOrderId(razorpayOrder.get("id"));
            rzp.setAmount(expected);
            rzp.setCurrency(request.getCurrency());
            rzp.setStatus("INITIATED");
            rzp.setCreatedAt(LocalDateTime.now());
            razorpayPaymentRepository.save(rzp);

            // Link back on Order
            order.setRazorpayOrderId(razorpayOrder.get("id"));
            orderRepository.save(order);

            return razorpayOrder.toJson();
        } catch (Exception e) {
            throw new RazorpayException("Payment initiation failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public JSONObject initiateExchangePayment(PaymentInitiateRequest request) throws RazorpayException {
        // Very similar to initiatePayment, but skip the OrderStatus check
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Don’t enforce PAYMENT_PENDING here – it’s a top-up payment
        BigDecimal amount = request.getAmount();
        JSONObject options = new JSONObject()
                .put("amount", amount.movePointRight(2).intValueExact())
                .put("currency", request.getCurrency())
                .put("receipt", "EXCHANGE-" + order.getOrderNumber())
                .put("notes", new JSONObject()
                        .put("order_id", order.getId().toString())
                        .put("type", "exchange"));

        com.razorpay.Order razorpayOrder = razorpayClient.orders.create(options);

        RazorpayPayment rzp = new RazorpayPayment();
        rzp.setOrder(order);
        rzp.setRazorpayOrderId(razorpayOrder.get("id"));
        rzp.setAmount(amount);
        rzp.setCurrency(request.getCurrency());
        rzp.setStatus("INITIATED");
        rzp.setCreatedAt(LocalDateTime.now());
        razorpayPaymentRepository.save(rzp);

        // You could persist an “ExchangePayment” record too if desired

        return razorpayOrder.toJson();
    }

    @Transactional
    @Override
    public Payment refundPayment(Long orderId) throws RazorpayException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getRazorpayPaymentId() == null) {
            throw new RuntimeException("No payment found for this order");
        }

        // compute refund = (discounted + tax) – shipping
        BigDecimal shipping = Optional.ofNullable(order.getShippingCost()).orElse(BigDecimal.ZERO);
        BigDecimal refundAmt = order.getTotalDiscountedPrice()
                .add(order.getTotalTaxAmount())
                .subtract(shipping)
                .setScale(2, RoundingMode.HALF_UP);

        try {
            // 1) Issue refund via Razorpay
            JSONObject refundRequest = new JSONObject()
                    .put("payment_id", order.getRazorpayPaymentId())
                    .put("amount", refundAmt.movePointRight(2).intValueExact())
                    .put("speed", "normal");

            com.razorpay.Refund refund = razorpayClient.payments.refund(refundRequest);

            // 2) Persist your own Payment record
            Payment payment = new Payment();
            payment.setOrderId(orderId);
            payment.setAmount(refundAmt);
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setTransactionId(refund.get("id"));
            paymentRepository.save(payment);

            // 3) Update Order status
            order.setPaymentStatus("REFUNDED");
            order.setOrderPaymentStatus(OrderPaymentStatus.REFUNDED);

            orderRepository.save(order);

            // 4) Update the RazorpayPayment row
            RazorpayPayment rzp = razorpayPaymentRepository
                    .findByOrder(order)
                    .orElseThrow(() -> new RuntimeException(
                            "RazorpayPayment record not found for order " + orderId));
            rzp.setRefundId(refund.get("id"));
            rzp.setRefundStatus(refund.get("status"));
            razorpayPaymentRepository.save(rzp);

            // 5) Update ReturnOrder with refund details
            ReturnOrder returnOrder = returnOrderRepository.findByOrderId(order.getId())
                    .orElseThrow(() -> new RuntimeException(
                            "ReturnOrder not found for order " + orderId));
            returnOrder.setRefundAmount(refundAmt);
            returnOrder.setRefundedAt(LocalDateTime.now());
            order.setOrderPaymentStatus(OrderPaymentStatus.REFUNDED);
            returnOrderRepository.save(returnOrder);

            return payment;

        } catch (RazorpayException e) {
            throw new RazorpayException("Refund failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    @Override
    public boolean verifyPayment(PaymentVerificationRequest request) throws RazorpayException {
        try {

            // Validate signature
            String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
            String expectedSig = PaymentUtils.calculateHMAC(payload, secretKey).toLowerCase();
            String actual = request.getRazorpaySignature().toLowerCase();
            if (!MessageDigest.isEqual(expectedSig.getBytes(), actual.getBytes())) {
                return false;
            }

            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

            // Update order with payment success
            order.setRazorpayPaymentId(request.getRazorpayPaymentId());
            order.setPaymentDate(LocalDateTime.now());
            order.setPaymentStatus("PAID");
            order.setOrderPaymentStatus(OrderPaymentStatus.PAYMENT_SUCCESS);
            order.getOrderPaymentStatusHistory().add(new OrderPaymentStatusHistory(
                order,
                OrderPaymentStatus.PAYMENT_SUCCESS,
                "Payment captured via webhook",
                LocalDateTime.now()
            ));
            
            orderRepository.save(order);

            // Persist domain Payment
            Payment p = new Payment();
            p.setOrderId(order.getId());
            p.setStatus(PaymentStatus.COMPLETED);
            p.setAmount(order.getTotalOrderPrice());
            p.setTransactionId(request.getRazorpayPaymentId());
            p.setUser(order.getUser());

            

            p.onCreate();
            paymentRepository.save(p);

            return true;
        } catch (Exception e) {
            throw new RazorpayException("Payment verification failed: " + e.getMessage());
        }
    }

    @Override
    public PaymentStatus checkPaymentStatus(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        try {
            com.razorpay.Payment payment = razorpayClient.payments.fetch(order.getRazorpayPaymentId());
            String status = payment.get("status");

            return switch (status) {
                case "created" -> PaymentStatus.PENDING;
                case "authorized" -> PaymentStatus.AUTHORIZED;
                case "captured" -> PaymentStatus.COMPLETED;
                case "failed" -> PaymentStatus.FAILED;
                case "refunded" -> PaymentStatus.REFUNDED;
                default -> PaymentStatus.UNKNOWN;
            };
        } catch (RazorpayException e) {
            throw new RuntimeException("Error checking payment status: " + e.getMessage());
        }
    }

    

    @Override
    public PaymentDTO getPaymentDetails(String paymentId) throws RazorpayException {
        try {
            com.razorpay.Payment rzpPayment = razorpayClient.payments.fetch(paymentId);
            JSONObject payment = rzpPayment.toJson();

            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setRazorpayPaymentId(payment.getString("id"));
            paymentDTO.setRazorpayOrderId(payment.getString("order_id"));
            paymentDTO.setStatus(payment.getString("status"));

            // Fix BigDecimal conversion
            long amountInPaise = payment.getLong("amount");
            BigDecimal amount = BigDecimal.valueOf(amountInPaise).divide(BigDecimal.valueOf(100));
            paymentDTO.setAmount(amount);

            paymentDTO.setCurrency(payment.getString("currency"));
            paymentDTO.setMethod(payment.getString("method"));

            // Fix card details handling
            if (payment.has("card") && !payment.isNull("card")) {
                JSONObject cardDetails = payment.getJSONObject("card");
                paymentDTO.setCardNetwork(cardDetails.getString("network"));
                paymentDTO.setCardLast4(cardDetails.getString("last4"));
            }

            // Fix refund details handling
            if (payment.has("refund_status") && !payment.isNull("refund_status")) {
                PaymentDTO.RefundDTO refundDTO = new PaymentDTO.RefundDTO();
                refundDTO.setStatus(payment.getString("refund_status"));

                // Fix refund amount conversion
                long refundAmountInPaise = payment.getLong("amount_refunded");
                BigDecimal refundAmount = BigDecimal.valueOf(refundAmountInPaise)
                        .divide(BigDecimal.valueOf(100));
                refundDTO.setAmount(refundAmount);

                paymentDTO.setRefund(refundDTO);
            }

            return paymentDTO;
        } catch (Exception e) {
            throw new RazorpayException("Error fetching payment details: " + e.getMessage());
        }
    }

    @Override
    public void capturePayment(String paymentId) throws RazorpayException {
        try {
            com.razorpay.Payment rzpPayment = razorpayClient.payments.fetch(paymentId);
            JSONObject payment = rzpPayment.toJson();

            JSONObject captureRequest = new JSONObject();
            captureRequest.put("amount", payment.getLong("amount")); // Use getLong for amount
            captureRequest.put("currency", payment.getString("currency")); // Use getString for currency

            razorpayClient.payments.capture(paymentId, captureRequest);
        } catch (Exception e) {
            throw new RazorpayException("Error capturing payment: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void initiateManualRefund(ReturnOrder returnOrder) {
        ReturnPaymentDetails details = returnOrder.getPaymentDetails();
        if (details == null) {
            throw new IllegalArgumentException("No payment details for return " + returnOrder.getId());
        }

        User user = returnOrder.getOrder().getUser();
        try {
            // 1) Contact
            String contactId = user.getRzpContactId();
            if (contactId == null) {
                JSONObject contact = xPayoutService.createOrFetchContact(
                    user.getFirstName() + " " + Optional.ofNullable(user.getLastName()).orElse(""),
                    user.getEmail(),
                    user.getPhone(),
                    "return-" + returnOrder.getId(),
                    Map.of("order", returnOrder.getOrder().getOrderNumber())
                );
                contactId = contact.getString("id");
                user.setRzpContactId(contactId);
                userRepository.save(user);
            }

            // 2) Fund account
            boolean isBank = details.getPaymentMethodType() == ReturnPaymentDetails.PaymentMethodType.BANK_TRANSFER;
            JSONObject fa = xPayoutService.createOrFetchFundAccount(
                contactId,
                isBank,
                details.getBankAccountHolderName(),
                details.getBankIfscCode(),
                details.getBankAccountNumber(),
                details.getUpiId()
            );
            String fundAccountId = fa.getString("id");

            // 3) Payout
            BigDecimal amt = returnOrder.getRefundAmount();
            long paise = amt.movePointRight(2).longValueExact();
            String referenceId = "ret-" + returnOrder.getId() + "-" + System.currentTimeMillis();
            JSONObject payout = xPayoutService.createPayout(
                fundAccountId,
                paise,
                isBank,
                referenceId,
                "Refund for order " + returnOrder.getOrder().getOrderNumber(),
                Map.of("returnOrderId", returnOrder.getId().toString())
            );
            String payoutId = payout.getString("id");

            // 4) Persist as a Payment
            Payment p = new Payment();
            p.setOrderId(returnOrder.getOrder().getId());
            p.setReturnOrderId(returnOrder.getId());
            p.setAmount(amt);
            p.setStatus(PaymentStatus.REFUND_INITIATED); 
            p.setTransactionId(payoutId);
            p.setUser(user);
            p.setPaymentMethod(details.getPaymentMethodType().name());
            p.onCreate();
            paymentRepository.save(p);

            // 5) Update return & order
            // returnOrder.setRefundedAt(LocalDateTime.now());
            returnOrder.setPaymentStatus(OrderPaymentStatus.REFUND_INITIATED);
            returnOrderRepository.save(returnOrder);

            Order order = returnOrder.getOrder();
            order.setOrderPaymentStatus(OrderPaymentStatus.REFUND_INITIATED);
            order.getOrderPaymentStatusHistory().add(new OrderPaymentStatusHistory(
                order, OrderPaymentStatus.REFUND_INITIATED,
                "Payout initiated, id=" + payoutId,
                LocalDateTime.now()));
            orderRepository.save(order);

        } catch (IOException | InterruptedException e) {
            log.error("Manual refund failed for return on paaymentServiceImpl.initiateManualRefund {}", returnOrder.getId(), e);
            throw new PaymentProcessingException("Manual refund error: " + e.getMessage(), e);
        }
    }


    @Override
    @Transactional
    public void initiateExchangeRefund(ExchangeOrder eo) {
        ExchangePaymentDetails details = eo.getPaymentDetails();
        if (details == null) {
            throw new IllegalArgumentException("No payment details for exchange order " + eo.getId());
        }

        User user = eo.getOrder().getUser();
        try {
            String contactId = user.getRzpContactId();
            if (contactId == null) {
                JSONObject contact = xPayoutService.createOrFetchContact(
                    user.getFirstName() + " " + Optional.ofNullable(user.getLastName()).orElse(""),
                    user.getEmail(),
                    user.getPhone(),
                    "exchange-" + eo.getId(),
                    Map.of("order_id", eo.getOrder().getId().toString())
                );
                contactId = contact.getString("id");
                user.setRzpContactId(contactId);
                userRepository.save(user);
            }

            boolean isBank = details.getPaymentMethodType() == ExchangePaymentDetails.PaymentMethodType.BANK_TRANSFER;
            JSONObject fa = xPayoutService.createOrFetchFundAccount(
                contactId,
                isBank,
                details.getBankAccountHolderName(),
                details.getBankIfscCode(),
                details.getBankAccountNumber(),
                details.getUpiId()
            );
            String fundAccountId = fa.getString("id");

            BigDecimal refundAmt = eo.getPriceDifference().abs();
            long paise = refundAmt.movePointRight(2).longValueExact();
            String referenceId = "exch-" + eo.getId() + "-" + System.currentTimeMillis();
            JSONObject payout = xPayoutService.createPayout(
                fundAccountId,
                paise,
                isBank,
                referenceId,
                "Exchange refund for order " + eo.getOrder().getOrderNumber(),
                Map.of("exchangeOrderId", eo.getId().toString())
            );
            String payoutId = payout.getString("id");

            Payment p = new Payment();
            p.setOrderId(eo.getOrder().getId());
            p.setExchangeOrderId(eo.getId());
            p.setAmount(refundAmt);
            p.setStatus(PaymentStatus.EXCHANGE_REFUND_INITIATED);
            p.setTransactionId(payoutId);
            p.setUser(user);
            p.setPaymentMethod(details.getPaymentMethodType().name());
            p.onCreate();
            paymentRepository.save(p);

            eo.setPayoutId(payoutId);
            eo.setStatus(ExchangeOrderStatus.EXCHANGE_REFUND_INITIATED);
            exchangeOrderRepository.save(eo);

            Order order = eo.getOrder();
            order.getOrderPaymentStatusHistory().add(new OrderPaymentStatusHistory(
                order,
                OrderPaymentStatus.EXCHANGE_REFUND_INITIATED,
                "Exchange refund initiated, payoutId=" + payoutId,
                LocalDateTime.now()
            ));
            orderRepository.save(order);
        } catch (Exception e) {
            log.error("Exchange refund failed for exchangeOrderId {}", eo.getId(), e);
            throw new RuntimeException("Exchange refund processing error: " + e.getMessage());
        }
    }



    @Override
    public String getSecretKey() {
        return secretKey;
    }

}
