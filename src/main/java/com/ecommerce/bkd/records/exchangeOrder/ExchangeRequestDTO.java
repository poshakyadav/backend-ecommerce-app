package com.ecommerce.bkd.records.exchangeOrder;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ExchangeRequestDTO(
    @NotEmpty List<ItemExchange> exchanges,
    @NotNull @Size(min = 1) String reason,
    String description,
    String paymentId,
    PaymentDetailsDTO paymentDetails
) {
    public record ItemExchange(
        @NotNull Long orderItemId,
        @NotNull Long replacementVariantId,
        @NotBlank String replacementSize
    ) {}

    public record PaymentDetailsDTO(
        @NotNull String paymentMethodType, // "BANK_TRANSFER" or "UPI"
        @NotNull String bankAccountHolderName,
        @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code") String bankIfscCode,
        @Pattern(regexp = "^[0-9]{9,18}$", message = "Invalid bank account number") String bankAccountNumber,
        @Pattern(regexp = "^[a-zA-Z0-9]+@[a-zA-Z0-9]+$", message = "Invalid UPI ID") String upiId
    ) {}
}