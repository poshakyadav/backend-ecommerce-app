package com.ecommerce.bkd.records;

import com.ecommerce.bkd.models.enums.PaymentMode;
import com.ecommerce.bkd.models.enums.ShippingMethod;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
                @NotNull Long userbillingAddressId, // Shipping and Billing address is same
                @NotNull @NotBlank String currency, // INR
                @Size(max = 255) String deliveryInstructions, // Order notes
                // Optional
                ShippingMethod shippingMethod,
                @NotNull PaymentMode paymentMode,
                @NotNull String contactPhone,
                String alternativePhone) {
}