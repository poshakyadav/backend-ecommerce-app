package com.ecommerce.bkd.services.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ecommerce.bkd.dtos.shiprocket.ShiprocketReturnOrderResponse;
import com.ecommerce.bkd.exceptions.ResourceNotFoundException;
import com.ecommerce.bkd.exceptions.ShiprocketApiException;
import com.ecommerce.bkd.models.Order;
import com.ecommerce.bkd.models.OrderReturnStatusHistory;
import com.ecommerce.bkd.models.ReturnOrder;
import com.ecommerce.bkd.models.enums.OrderReturnStatus;
import com.ecommerce.bkd.records.returnOrder.ReturnOrderDTO;
import com.ecommerce.bkd.repositories.OrderRepository;
import com.ecommerce.bkd.repositories.ReturnOrderRepository;
import com.ecommerce.bkd.services.ReturnOrderMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminReturnService {

    private final ReturnOrderRepository returnRepo;
    private final OrderRepository orderRepo;
    private final ShiprocketReturnExchangeServiceImpl shiprocketService;
    private final ReturnOrderMapper mapper;

    public List<ReturnOrderDTO> listPendingReturns() {
        return returnRepo.findByReturnStatus(OrderReturnStatus.RETURN_REQUESTED).stream()
                .map(mapper::toReturnOrderDTO).toList();
    }

    @Transactional
    public ReturnOrderDTO approveReturn(Long returnOrderId) {
        ReturnOrder ro = returnRepo.findById(returnOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Return order not found"));

        Order order = ro.getOrder();
        ro.setReturnStatus(OrderReturnStatus.RETURN_APPROVED);
        ro.setApprovedAt(LocalDateTime.now());
        returnRepo.save(ro);

        order.setReturnStatus(OrderReturnStatus.RETURN_APPROVED);
        order.getOrderReturnStatusHistory().add(new OrderReturnStatusHistory(
                order,
                OrderReturnStatus.RETURN_APPROVED,
                "Return approved by admin",
                LocalDateTime.now()
        ));
        orderRepo.save(order);

        try {
            // Create return order on Shiprocket
            ShiprocketReturnOrderResponse sr = shiprocketService.createReturnOrder(
                    mapper.toShiprocketReturnOrderRequest(ro));

            ro.setShiprocketReturnOrderId(sr.getOrderId());
            ro.setShiprocketReturnShipmentId(sr.getShipmentId());
            ro.setReturnStatus(OrderReturnStatus.RETURN_INITIATED);

            returnRepo.save(ro);

            order.getOrderReturnStatusHistory().add(new OrderReturnStatusHistory(
                    order,
                    OrderReturnStatus.RETURN_INITIATED,
                    "Return initiated with Shiprocket",
                    LocalDateTime.now()
            ));

            orderRepo.save(order);

            // Generate AWB for return shipment or  Ask Shiprocket to assign AWB (they'll do it asynchronously)
            shiprocketService.generateReturnAwb(
                    String.valueOf(sr.getShipmentId()),
                    null, // courier_id (default courier)
                    null,   // status
                    true  // is_return
            );
            ro.setReturnStatus(OrderReturnStatus.RETURN_AWB_PENDING);
            returnRepo.save(ro);

            order.getOrderReturnStatusHistory().add(new OrderReturnStatusHistory(
                    order,
                    OrderReturnStatus.RETURN_AWB_PENDING,
                    "Requested AWB generation for return shipment, awaiting AWB",
                    LocalDateTime.now()
            ));

            orderRepo.save(order);
        } catch (ShiprocketApiException e) {
            ro.setReturnStatus(OrderReturnStatus.RETURN_APPROVED_FAILED);
            ro.setRejectReason("Shiprocket API error: " + e.getMessage());
            ro.setRejectedAt(LocalDateTime.now());
            order.getOrderReturnStatusHistory().add(new OrderReturnStatusHistory(
                    order,
                    OrderReturnStatus.RETURN_APPROVED_FAILED,
                    "Return approved failed due to Shiprocket API error",
                    LocalDateTime.now()
            ));
            returnRepo.save(ro);
            orderRepo.save(order);
            throw e;
        }

        return mapper.toReturnOrderDTO(ro);
    }

    @Transactional
    public ReturnOrderDTO rejectReturn(Long returnOrderId, String reason) {
        ReturnOrder ro = returnRepo.findById(returnOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Return order not found"));
        Order order = ro.getOrder();
        ro.setReturnStatus(OrderReturnStatus.RETURN_REJECTED);
        ro.setRejectReason(reason);
        ro.setRejectedAt(LocalDateTime.now());
        returnRepo.save(ro);

        order.setReturnStatus(OrderReturnStatus.RETURN_REJECTED);
        order.getOrderReturnStatusHistory().add(new OrderReturnStatusHistory(
                order,
                OrderReturnStatus.RETURN_REJECTED,
                "Return rejected by admin: " + reason,
                LocalDateTime.now()
        ));
        orderRepo.save(order);

        return mapper.toReturnOrderDTO(ro);
    }
}
