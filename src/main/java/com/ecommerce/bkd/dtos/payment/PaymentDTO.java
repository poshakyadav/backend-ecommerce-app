package com.ecommerce.bkd.dtos.payment;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentDTO {
    private String razorpayPaymentId;
    private String razorpayOrderId;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String method;
    private String cardNetwork;
    private String cardLast4;
    private LocalDateTime createdAt;
    private String errorCode;
    private String errorDescription;
    private RefundDTO refund;

    @Data
    public static class RefundDTO {
        private String refundId;
        private BigDecimal amount;
        private String status;
        private LocalDateTime processedAt;
    }
}
