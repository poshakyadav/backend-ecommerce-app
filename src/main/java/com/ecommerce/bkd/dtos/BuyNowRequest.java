package com.ecommerce.bkd.dtos;

import com.ecommerce.bkd.models.enums.PaymentMode;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BuyNowRequest {
    @NotNull
    private Long productId;
    
    @NotNull
    @Min(1)
    private Integer quantity;
    
    @NotNull
    private Long shippingAddressId;
    
    // Make it optional
    private Long paymentMethodId;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;
    
    private String orderNotes;
    private boolean savePaymentMethod = true;

    public boolean isPaymentMethodCod() {
        return PaymentMode.COD.equals(paymentMode);
    }
}
