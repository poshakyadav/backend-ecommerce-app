package com.ecommerce.bkd.records.order;

import com.ecommerce.bkd.models.enums.PaymentMode;
import com.ecommerce.bkd.models.enums.ShippingMethod;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateDirectOrderRequest(
    @NotNull Long userbillingAddressId,
    @NotNull @NotBlank String currency,
    @Size(max = 255) String deliveryInstructions,
    ShippingMethod shippingMethod,
    @NotNull PaymentMode paymentMode,
    @NotNull String contactPhone,
    String alternativePhone,
    @NotNull Long variantId,
    @NotNull String selectedSize,
    @Min(1) int quantity
) {}