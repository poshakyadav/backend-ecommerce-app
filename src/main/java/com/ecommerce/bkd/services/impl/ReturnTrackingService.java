package com.ecommerce.bkd.services.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.ecommerce.bkd.dtos.shiprocket.ShiprocketWebhookEvent;
import com.ecommerce.bkd.exceptions.PaymentProcessingException;
import com.ecommerce.bkd.exceptions.ShiprocketApiException;
import com.ecommerce.bkd.models.Order;
import com.ecommerce.bkd.models.OrderPaymentStatusHistory;
import com.ecommerce.bkd.models.OrderReturnStatusHistory;
import com.ecommerce.bkd.models.ReturnOrder;
import com.ecommerce.bkd.models.enums.OrderPaymentStatus;
import com.ecommerce.bkd.models.enums.OrderReturnStatus;
import com.ecommerce.bkd.repositories.OrderRepository;
import com.ecommerce.bkd.repositories.ReturnOrderRepository;
import com.ecommerce.bkd.services.PaymentService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReturnTrackingService {

    private final ReturnOrderRepository returnRepo;
    private final OrderRepository orderRepo;
    private final PaymentService paymentService;

    /**
     * Handles Shiprocket webhook events for return shipments.
     */
    @Transactional
    public void handleReturnEvent(ShiprocketWebhookEvent event) {
        String awb = event.getAwb();
        LocalDateTime timestamp = parseTimestamp(event.getCurrentTimestamp());

        ReturnOrder ro = returnRepo.findByShiprocketReturnAwbCode(awb)
                .orElseThrow(() -> new ShiprocketApiException("Return order not found for AWB " + awb));

        Order order = ro.getOrder();
        String currentStatus = event.getCurrentStatus().toUpperCase();

        log.info("ReturnTracking: AWB={} status={} at {}", awb, currentStatus, timestamp);

        //  If we were just waiting on the AWB, this webhook delivers it
        if (ro.getReturnStatus() == OrderReturnStatus.RETURN_AWB_PENDING) {
            ro.setShiprocketReturnAwbCode(awb);
            ro.setReturnStatus(OrderReturnStatus.RETURN_AWB_GENERATED);
            
            // returnRepo.save(ro);

            order.getOrderReturnStatusHistory().add(new OrderReturnStatusHistory(
                    order,
                    OrderReturnStatus.RETURN_AWB_GENERATED,
                    "AWB assigned by Shiprocket: " + awb,
                    timestamp
            ));
            // orderRepo.save(order);

            // After AWB generation we still want to handle the next lifecycle states,
            // so we fall-through to the mapping below.
        }

        switch (currentStatus) {
            case "RETURN PENDING" -> {
                ro.setReturnStatus(OrderReturnStatus.RETURN_INITIATED);
                order.getOrderReturnStatusHistory().add(new OrderReturnStatusHistory(
                        order,
                        OrderReturnStatus.RETURN_INITIATED,
                        "Return pending with Shiprocket",
                        timestamp
                ));
            }
            case "RETURN IN TRANSIT" -> {
                ro.setReturnStatus(OrderReturnStatus.RETURN_IN_TRANSIT);
                order.getOrderReturnStatusHistory().add(new OrderReturnStatusHistory(
                        order,
                        OrderReturnStatus.RETURN_IN_TRANSIT,
                        "Return shipment in transit",
                        timestamp
                ));
            }
            case "RETURNED", "RETURNED DELIVERED" -> processReturned(ro, order, timestamp);
            default -> log.debug("Unhandled return status: {}", currentStatus);
        }

        returnRepo.save(ro);
        orderRepo.save(order);
    }

    /**
    * Processes final "RETURNED" status: mark returned and issue refund.
    */
    private void processReturned(ReturnOrder ro, Order order, LocalDateTime ts) {
        ro.setReturnStatus(OrderReturnStatus.RETURNED);
        ro.setReturnedAt(ts);
        order.getOrderReturnStatusHistory().add(new OrderReturnStatusHistory(
                order,
                OrderReturnStatus.RETURNED,
                "Item returned to seller",
                ts
        ));

        try {
            // Initiate manual refund via Razorpay X Payout for all orders
            paymentService.initiateManualRefund(ro);
        } catch (PaymentProcessingException e) {
            log.error("Refund failed for order {}: {}", order.getId(), e.getMessage());
            ro.setPaymentStatus(OrderPaymentStatus.REFUND_FAILED);
            order.setOrderPaymentStatus(OrderPaymentStatus.REFUND_FAILED);
            order.getOrderPaymentStatusHistory().add(new OrderPaymentStatusHistory(
                order,
                OrderPaymentStatus.REFUND_FAILED,
                "Refund failed: " + e.getMessage(),
                ts
            ));
        } catch (Exception e) {
            log.error("Refund failed for order {}: {}", order.getId(), e.getMessage());
            ro.setPaymentStatus(OrderPaymentStatus.REFUND_FAILED);
            order.setOrderPaymentStatus(OrderPaymentStatus.REFUND_FAILED);
            order.getOrderPaymentStatusHistory().add(new OrderPaymentStatusHistory(
                    order,
                    OrderPaymentStatus.REFUND_FAILED,
                    "Refund failed: " + e.getMessage(),
                    ts
            ));
        }

        returnRepo.save(ro);
        orderRepo.save(order);
    }


    /**
     * Parses Shiprocket's timestamp format "dd MM yyyy HH:mm:ss" into
     * LocalDateTime.
     */
    private LocalDateTime parseTimestamp(String ts) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MM yyyy HH:mm:ss");
        return LocalDateTime.parse(ts, fmt);
    }
}
