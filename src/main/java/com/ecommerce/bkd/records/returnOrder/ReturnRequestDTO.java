package com.ecommerce.bkd.records.returnOrder;

import com.ecommerce.bkd.models.ReturnPaymentDetails;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReturnRequestDTO(
    @NotNull(message = "Reason is required")
    @Size(min = 5, max = 200, message = "Reason must be between 5 and 200 characters")
    String reason,

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description,

    @NotNull(message = "Payment details are required for refund")
    ReturnPaymentDetailsDTO paymentDetails
) {
    public record ReturnPaymentDetailsDTO(
        @NotNull(message = "Payment method type is required")
        ReturnPaymentDetails.PaymentMethodType paymentMethodType,
        String bankAccountNumber,
        String bankIfscCode,
        String bankAccountHolderName,
        String upiId
    ) {
        public void validate() {
            switch (paymentMethodType) {
                case BANK_TRANSFER:
                    if (bankAccountNumber == null || bankAccountNumber.isBlank() || !bankAccountNumber.matches("^[0-9]{9,18}$")) {
                        throw new IllegalArgumentException("Invalid or missing bank account number (must be 9-18 digits)");
                    }
                    if (bankIfscCode == null || bankIfscCode.isBlank() || !bankIfscCode.matches("^[A-Z]{4}0[A-Z0-9]{6}$")) {
                        throw new IllegalArgumentException("Invalid or missing IFSC code (e.g., SBIN0001234)");
                    }
                    if (bankAccountHolderName == null || bankAccountHolderName.isBlank() || bankAccountHolderName.length() < 3 || bankAccountHolderName.length() > 100) {
                        throw new IllegalArgumentException("Invalid or missing account holder name (must be 3-100 characters)");
                    }
                    if (upiId != null && !upiId.isBlank()) {
                        throw new IllegalArgumentException("UPI ID should not be provided for BANK_TRANSFER");
                    }
                    break;
                case UPI:
                    if (upiId == null || upiId.isBlank() || !upiId.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9]+$")) {
                        throw new IllegalArgumentException("Invalid or missing UPI ID (e.g., user@bank)");
                    }
                    if (bankAccountNumber != null || bankIfscCode != null || bankAccountHolderName != null) {
                        throw new IllegalArgumentException("Bank details should not be provided for UPI");
                    }
                    break;
            }
        }
    }
}