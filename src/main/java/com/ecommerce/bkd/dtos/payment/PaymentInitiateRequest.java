package com.ecommerce.bkd.dtos.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiateRequest {
    private Long orderId;
    private BigDecimal amount;
    private String currency;
}
