package com.ecommerce.bkd.dtos.payment;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PaymentValidationRequest {
    @NotNull
    private Long orderId;

    @NotNull
    @Positive
    private BigDecimal amount;

    private String currency;
}
