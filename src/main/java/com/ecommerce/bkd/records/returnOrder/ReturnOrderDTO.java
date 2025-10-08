package com.ecommerce.bkd.records.returnOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ecommerce.bkd.models.ReturnPaymentDetails;
import com.ecommerce.bkd.models.enums.OrderPaymentStatus;
import com.ecommerce.bkd.models.enums.OrderReturnStatus;

public record ReturnOrderDTO(
        Long id,
        Long orderId,
        OrderReturnStatus returnStatus,
        OrderPaymentStatus paymentStatus,
        String reason,
        String description,
        String rejectReason,
        BigDecimal refundAmount,
        LocalDateTime refundedAt,
        Long shiprocketReturnOrderId,
        Long shiprocketReturnShipmentId,
        String shiprocketReturnAwbCode,
        ReturnPaymentDetailsDTO paymentDetails,
        LocalDateTime requestedAt,
        LocalDateTime approvedAt,
        LocalDateTime rejectedAt,
        LocalDateTime returnedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record ReturnPaymentDetailsDTO(
            Long id,
            ReturnPaymentDetails.PaymentMethodType paymentMethodType,
            String bankAccountNumber,
            String bankIfscCode,
            String bankAccountHolderName,
            String upiId
    ) {}
}