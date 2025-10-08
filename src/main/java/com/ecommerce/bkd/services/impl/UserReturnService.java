package com.ecommerce.bkd.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.ecommerce.bkd.exceptions.ResourceNotFoundException;
import com.ecommerce.bkd.models.Order;
import com.ecommerce.bkd.models.OrderReturnStatusHistory;
import com.ecommerce.bkd.models.ReturnOrder;
import com.ecommerce.bkd.models.ReturnPaymentDetails;
import com.ecommerce.bkd.models.enums.OrderFulfillmentStatus;
import com.ecommerce.bkd.models.enums.OrderPaymentStatus;
import com.ecommerce.bkd.models.enums.OrderReturnStatus;
import com.ecommerce.bkd.records.returnOrder.ReturnOrderDTO;
import com.ecommerce.bkd.records.returnOrder.ReturnRequestDTO;
import com.ecommerce.bkd.repositories.OrderRepository;
import com.ecommerce.bkd.repositories.ReturnOrderRepository;
import com.ecommerce.bkd.repositories.ReturnPaymentDetailsRepository;
import com.ecommerce.bkd.services.ReturnOrderMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserReturnService {

    private final OrderRepository orderRepo;
    private final ReturnOrderRepository returnRepo;
    private final ReturnPaymentDetailsRepository paymentDetailsRepo;
    private final ReturnOrderMapper mapper;

    @Transactional
    public ReturnOrderDTO requestReturn(Long userId, Long orderId, ReturnRequestDTO req) {
        Order order = orderRepo.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Validate order status
        if (order.getDeliveredAt() == null || order.getFulfillmentStatus() != OrderFulfillmentStatus.DELIVERED) {
            throw new IllegalStateException("Order must be delivered before a return can be requested");
        }

        // Enforce 7-day window
        if (order.getDeliveredAt().plusDays(7).isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Return window has expired (7 days after delivery)");
        }

        // Only one return per order
        if (returnRepo.findByOrderId(orderId).isPresent()) {
            throw new IllegalStateException("Return already requested for this order");
        }

        // Validate payment details for all orders
        if (req.paymentDetails() == null) {
            throw new IllegalStateException("Payment details are required for all return requests");
        }

        // Create ReturnOrder
        ReturnOrder ro = new ReturnOrder();
        ro.setOrder(order);
        ro.setReturnStatus(OrderReturnStatus.RETURN_REQUESTED);
        ro.setReason(req.reason());
        ro.setDescription(req.description());
        ro.setRequestedAt(LocalDateTime.now());
        ro.setPaymentStatus(OrderPaymentStatus.REFUND_PENDING);

        // Calculate refund amount
        ro.setRefundAmount(order.getTotalDiscountedPrice()
                .add(order.getTotalTaxAmount())
                .subtract(order.getShippingCost() != null ? order.getShippingCost() : BigDecimal.ZERO));

        // Save payment details for all orders
        ReturnPaymentDetails paymentDetails = new ReturnPaymentDetails();
        paymentDetails.setReturnOrder(ro);
        paymentDetails.setPaymentMethodType(req.paymentDetails().paymentMethodType());
        paymentDetails.setBankAccountNumber(req.paymentDetails().bankAccountNumber());
        paymentDetails.setBankIfscCode(req.paymentDetails().bankIfscCode());
        paymentDetails.setBankAccountHolderName(req.paymentDetails().bankAccountHolderName());
        paymentDetails.setUpiId(req.paymentDetails().upiId());
        ro.setPaymentDetails(paymentDetails);
        paymentDetailsRepo.save(paymentDetails);

        returnRepo.save(ro);
        order.getOrderReturnStatusHistory().add(new OrderReturnStatusHistory(
                order,
                OrderReturnStatus.RETURN_REQUESTED,
                "Return requested by user",
                LocalDateTime.now()
        ));
        orderRepo.save(order);

        return mapper.toReturnOrderDTO(ro);
    }
}