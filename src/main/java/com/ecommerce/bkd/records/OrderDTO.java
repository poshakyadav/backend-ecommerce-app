package com.ecommerce.bkd.records;

import java.math.BigDecimal;
import java.util.List;

import com.ecommerce.bkd.models.enums.OrderFulfillmentStatus;
import com.ecommerce.bkd.models.enums.OrderPaymentStatus;
import com.ecommerce.bkd.models.enums.PaymentMode;

public record OrderDTO(
        Long id,
        String orderNumber,
        OrderPaymentStatus paymentStatus,
        OrderFulfillmentStatus fulfillmentStatus,
        BigDecimal total, 
        String razorpayOrderId,
        String shiprocketOrderId,
        String shiprocketAwbNumber,
        List<OrderItemDTO> items,
        PaymentMode paymentMode,

        BigDecimal tax,
        BigDecimal shippingCost,
        BigDecimal totalDiscount
        ) {
}