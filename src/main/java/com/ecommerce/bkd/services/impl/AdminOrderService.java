package com.ecommerce.bkd.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.bkd.dtos.shiprocket.AwbAssignWrapper;
import com.ecommerce.bkd.dtos.shiprocket.AwbData;
import com.ecommerce.bkd.dtos.shiprocket.CourierServiceability;
import com.ecommerce.bkd.dtos.shiprocket.CreateOrderResponse;
import com.ecommerce.bkd.dtos.shiprocket.LabelResponse;
import com.ecommerce.bkd.dtos.shiprocket.ManifestResponse;
import com.ecommerce.bkd.dtos.shiprocket.PickupAssignWrapper;
import com.ecommerce.bkd.dtos.shiprocket.ServiceabilityData;
import com.ecommerce.bkd.dtos.shiprocket.ShiprocketResponse;
import com.ecommerce.bkd.exceptions.ResourceNotFoundException;
import com.ecommerce.bkd.exceptions.ShiprocketApiException;
import com.ecommerce.bkd.models.Order;
import com.ecommerce.bkd.models.OrderFulfillmentStatusHistory;
import com.ecommerce.bkd.models.OrderPaymentStatusHistory;
import com.ecommerce.bkd.models.enums.OrderFulfillmentStatus;
import com.ecommerce.bkd.models.enums.OrderPaymentStatus;
import com.ecommerce.bkd.records.AwbRequest;
import com.ecommerce.bkd.records.CourierSelectionRequest;
import com.ecommerce.bkd.records.OrderDTO;
import com.ecommerce.bkd.records.PickupRequest;
import com.ecommerce.bkd.repositories.OrderRepository;
import com.ecommerce.bkd.services.OrderMapper;
import com.ecommerce.bkd.services.PaymentService;
import com.ecommerce.bkd.services.ShiprocketLMIService;
import com.razorpay.RazorpayException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOrderService {
    private final OrderRepository orderRepo;
    private final ShiprocketCourierService courierService;
    private final ShiprocketOrderService shiprocketOrderService;
    private final PaymentService paymentService;
    private final ShiprocketLMIService lmiService;
    private final OrderMapper mapper;


    /**
     * 1) Accept a single order: set fulfillmentStatus = ACCEPTED.
     */
    @Transactional
    public OrderDTO acceptOrder(Long orderId) {
        Order o = findOrder(orderId);
        o.setFulfillmentStatus(OrderFulfillmentStatus.ACCEPTED);
        o.getOrderFulfillmentStatusHistory().add(
            new OrderFulfillmentStatusHistory(o,
                OrderFulfillmentStatus.ACCEPTED,
                "Order accepted by admin",
                LocalDateTime.now())
        );
        orderRepo.save(o);
        return mapper.toOrderDTO(o);
    }

    /**
     * 2) AUTO‐ASSIGN couriers to ALL orders currently in ACCEPTED status.
     *    Returns a List<OrderDTO> of all processed orders (either COURIER_SELECTED or SERVICEABILITY_CHECK_FAILED).
     */
    @Transactional
    public List<OrderDTO> autoAssignCouriersForAllAccepted() {
        // Fetch all orders with status = ACCEPTED
        List<Order> toAssign = orderRepo.findByFulfillmentStatus(OrderFulfillmentStatus.ACCEPTED);

        for (Order o : toAssign) {
            tryAutoAssign(o);
        }

        // Save all changes in one batch
        orderRepo.saveAll(toAssign);

        // Return updated DTOs
        return toAssign.stream()
                .map(mapper::toOrderDTO)
                .collect(Collectors.toList());
    }

     /**
     * Helper: attempt to auto-assign a “best courier” for a single order.
     *   - If serviceability returns no options, sets status = SERVICEABILITY_CHECK_FAILED.
     *   - Otherwise picks best by (rate + (cod?cod_charges:0)), then ETA tie-breaker.
     */
    private void tryAutoAssign(Order o) {
        try {
            // 1. Call Shiprocket Serviceability
            ShiprocketResponse<ServiceabilityData> resp = courierService
                    .checkServiceability(
                        mapper.getPickupPincode(),
                        mapper.getShippingPincode(o),
                        o.getPackageWeight(),
                        o.isCodOrder(),
                        o.getShiprocketOrderId() != null
                            ? o.getShiprocketOrderId().toString()
                            : null,
                        /* isReturn? */ false,
                        /* declaredValue? */ null,
                        /* length? */ optionalInt(o.getPackageLength()),
                        /* breadth? */ optionalInt(o.getPackageBreadth()),
                        /* height? */ optionalInt(o.getPackageHeight())
                    );

            List<CourierServiceability> options = resp
                    .getData()
                    .getAvailableCourierCompanies();

            if (options.isEmpty()) {
                // No courier can service → mark for manual review
                o.setFulfillmentStatus(OrderFulfillmentStatus.SERVICEABILITY_CHECK_FAILED);
                o.getOrderFulfillmentStatusHistory().add(
                new OrderFulfillmentStatusHistory(o,
                OrderFulfillmentStatus.SERVICEABILITY_CHECK_FAILED,
                "No couriers available for serviceability, so setting status SERVICEABILITY_CHECK_FAILED",
                LocalDateTime.now())
                );
                o.setErrorCode("NO_SERVICEABILITY");
                o.setErrorMessage("No couriers returned by serviceability");
                return;
            }

            // 2. Filter and sort to pick “best” courier
            Optional<CourierServiceability> bestOpt = options.stream()
                    // If COD order → keep only cod-enabled couriers
                    .filter(c -> !o.isCodOrder() || c.getCod() == 1)
                    // Compute totalCost = rate (+ cod_charges if COD)
                    .sorted(Comparator
                        .comparing((CourierServiceability c) ->
                            c.getRate()
                             .add(o.isCodOrder()
                                  ? c.getCodCharges()
                                  : BigDecimal.ZERO)
                        )
                        // tie-breaker: fewer ETA days
                        .thenComparing(CourierServiceability::getEstimatedDeliveryDays)
                    )
                    .findFirst();

            if (bestOpt.isEmpty()) {
                // No matching courier after filtering
                o.setFulfillmentStatus(OrderFulfillmentStatus.SERVICEABILITY_CHECK_FAILED);
                o.getOrderFulfillmentStatusHistory().add(
                new OrderFulfillmentStatusHistory(o,
                OrderFulfillmentStatus.SERVICEABILITY_CHECK_FAILED,
                "No matching courier after filtering, so setting status SERVICEABILITY_CHECK_FAILED",
                LocalDateTime.now())
                );
                o.setErrorCode("NO_COD_MATCH");
                o.setErrorMessage("No COD‐eligible courier available");
                return;
            }

            CourierServiceability best = bestOpt.get();
            BigDecimal bestRate = best.getRate()
                                     .add(o.isCodOrder()
                                          ? best.getCodCharges()
                                          : BigDecimal.ZERO);

            // 3. (Optional) Threshold check: if bestRate is unexpectedly high (e.g. > ₹500),
            // we might want to flag for human review. Uncomment and tune if needed:
            //
            // if (bestRate.compareTo(new BigDecimal("500")) > 0) {
            //     o.setStatus(OrderStatus.SERVICEABILITY_CHECK_FAILED);
            //     o.setErrorCode("RATE_TOO_HIGH");
            //     o.setErrorMessage("Auto‐selected courier charge (" + bestRate + ") exceeds threshold");
            //     return;
            // }

            // 4. Assign this courier to the order
            o.setShiprocketCourierCompanyId(best.getCourierCompanyId());
            o.setFulfillmentStatus(OrderFulfillmentStatus.COURIER_SELECTED);
            o.getOrderFulfillmentStatusHistory().add(
                new OrderFulfillmentStatusHistory(o,
                    OrderFulfillmentStatus.COURIER_SELECTED,
                    "Auto‐assigned courier " + best.getCourierName() +
                    " with rate " + bestRate + " and ETA " + best.getEstimatedDeliveryDays() + " days",
                    LocalDateTime.now())
            );
            // (We leave the actual "generate AWB" for the next step in Admin's workflow)

        } catch (Exception ex) {
            // Any exception → manual intervention required
            o.setFulfillmentStatus(OrderFulfillmentStatus.SERVICEABILITY_CHECK_FAILED);
            o.getOrderFulfillmentStatusHistory().add(
                new OrderFulfillmentStatusHistory(o,
                    OrderFulfillmentStatus.SERVICEABILITY_CHECK_FAILED,
                    "Serviceability check failed with exception: " + ex.getMessage(),
                    LocalDateTime.now())
            );
            o.setErrorCode("AUTO_ASSIGN_ERROR");
            o.setErrorMessage(ex.getMessage());
            log.error("Auto-assign failed for order {}: {}", o.getId(), ex.getMessage());
        }
    }

    
    /**
     * 3) FETCH available couriers for a single order (for “Override” button).
     */
    @Transactional(readOnly = true)
    public List<CourierServiceability> fetchAvailableCouriers(Long orderId) {
        Order o = findOrder(orderId);

        // Re‐run serviceability (no override of dimensions here—just fresh call)
        ShiprocketResponse<ServiceabilityData> resp = courierService
                .checkServiceability(
                    mapper.getPickupPincode(),
                    mapper.getShippingPincode(o),
                    o.getPackageWeight(),
                    o.isCodOrder(),
                    o.getShiprocketOrderId() != null
                        ? o.getShiprocketOrderId().toString()
                        : null,
                    /* isReturn? */ false,
                    /* declaredValue? */ null,
                    optionalInt(o.getPackageLength()),
                    optionalInt(o.getPackageBreadth()),
                    optionalInt(o.getPackageHeight())
                );

        List<CourierServiceability> options = resp
                .getData()
                .getAvailableCourierCompanies();

        if (options.isEmpty()) {
            throw new ShiprocketApiException("No courier options available for override");
        }

        return options;
    }


    /**
     * 4) OVERRIDE courier selection for one order.
     *    Caller must supply a valid courierId. We check that it appears in the
     *    fresh serviceability response.
     */
    @Transactional
    public OrderDTO overrideSelectCourier(Long orderId, CourierSelectionRequest req) {
        Order o = findOrder(orderId);

        // 1) Re‐run serviceability to get the fresh list
        List<CourierServiceability> options = fetchAvailableCouriers(orderId);

        boolean found = options.stream()
                .anyMatch(c -> c.getCourierCompanyId().equals(req.courierId()));
        if (!found) {
            throw new ShiprocketApiException("Requested courierId " + req.courierId() + 
                " is not valid for this order's serviceability");
        }

        // 2) Override selection
        o.setShiprocketCourierCompanyId(req.courierId());
        o.setFulfillmentStatus(OrderFulfillmentStatus.COURIER_SELECTED);
        o.getOrderFulfillmentStatusHistory().add(
            new OrderFulfillmentStatusHistory(o,
                OrderFulfillmentStatus.COURIER_SELECTED,
                "Courier overridden to " + req.courierId(),
                LocalDateTime.now())
        );
        orderRepo.save(o);

        return mapper.toOrderDTO(o);
    }



     /**
     * 5) Existing single‐order generate AWB. We add a try/catch so that if 
     * a manual “generate‐AWB” fails, we also set SR_AWB_GENERATION_FAILED.
     */
    @Transactional
    public OrderDTO generateAwb(Long orderId, AwbRequest req) {
        Order o = findOrder(orderId);

        try {
            AwbAssignWrapper awbWrapper = courierService.generateAWB(
                o.getShiprocketShipmentId(),
                req.courierId()
            );
            AwbData data = awbWrapper.getResponse().getData();
            if (data == null || data.getAwbCode() == null) {
                o.setFulfillmentStatus(OrderFulfillmentStatus.SR_AWB_GENERATION_FAILED);
                o.getOrderFulfillmentStatusHistory().add(
                new OrderFulfillmentStatusHistory(o,
                OrderFulfillmentStatus.SR_AWB_GENERATION_FAILED,
                "No awb_code returned in Shiprocket response, so setting status SR_AWB_GENERATION_FAILED",
                LocalDateTime.now())
                );
                o.setErrorCode("AWB_NO_CODE");
                o.setErrorMessage("No awb_code returned in Shiprocket response");
            } else {
                o.setShiprocketAwbNumber(data.getAwbCode());
                o.setShiprocketCourierName(data.getCourierName());
                o.setFulfillmentStatus(OrderFulfillmentStatus.AWB_GENERATED);
                o.getOrderFulfillmentStatusHistory().add(
                    new OrderFulfillmentStatusHistory(o,
                        OrderFulfillmentStatus.AWB_GENERATED,
                        "AWB generated successfully: " + data.getAwbCode(),
                        LocalDateTime.now())
                );
            }
        } catch (Exception ex) {
            o.setFulfillmentStatus(OrderFulfillmentStatus.SR_AWB_GENERATION_FAILED);
            o.getOrderFulfillmentStatusHistory().add(
                new OrderFulfillmentStatusHistory(o,
                    OrderFulfillmentStatus.SR_AWB_GENERATION_FAILED,
                    "AWB generation failed with exception: " + ex.getMessage(),
                    LocalDateTime.now())
            );
            o.setErrorCode("SR_AWB_GENERATION_FAILED");
            o.setErrorMessage(ex.getMessage());
            log.error("Manual AWB generation failed for order {}: {}", orderId, ex.getMessage());
        }

        orderRepo.save(o);
        return mapper.toOrderDTO(o);
    }

    /**
     * 6) AUTO‐GENERATE AWB for ALL orders currently in COURIER_SELECTED.
     *     - Orders that succeed move to AWB_GENERATED.
     *     - Orders that fail move to SR_AWB_GENERATION_FAILED.
     */
    @Transactional
    public List<OrderDTO> autoGenerateAwbForAllCourierSelected() {
        // 1. Fetch all orders whose status == COURIER_SELECTED
        List<Order> toProcess = orderRepo.findByFulfillmentStatus(OrderFulfillmentStatus.COURIER_SELECTED);

        for (Order o : toProcess) {
            try {
                // 2. Call Shiprocket’s generate AWB API
                AwbAssignWrapper awbWrapper = courierService.generateAWB(
                    o.getShiprocketShipmentId(),
                    o.getShiprocketCourierCompanyId()
                );

                AwbData data = awbWrapper.getResponse().getData();
                if (data == null || data.getAwbCode() == null) {
                    // Unexpected: no AWB code in the JSON
                    o.setFulfillmentStatus(OrderFulfillmentStatus.SR_AWB_GENERATION_FAILED);
                    o.getOrderFulfillmentStatusHistory().add(
                        new OrderFulfillmentStatusHistory(o,
                            OrderFulfillmentStatus.SR_AWB_GENERATION_FAILED,
                            "No awb_code returned in Shiprocket response, so setting status SR_AWB_GENERATION_FAILED",
                            LocalDateTime.now())
                    );
                    o.setErrorCode("AWB_NO_CODE");
                    o.setErrorMessage("No awb_code returned in Shiprocket response");
                } else {
                    // 3. Persist AWB / courier‐name, update status
                    o.setShiprocketAwbNumber(data.getAwbCode());
                    o.setShiprocketCourierName(data.getCourierName());
                    o.setFulfillmentStatus(OrderFulfillmentStatus.AWB_GENERATED);
                    o.getOrderFulfillmentStatusHistory().add(
                        new OrderFulfillmentStatusHistory(o,
                            OrderFulfillmentStatus.AWB_GENERATED,
                            "AWB generated successfully: " + data.getAwbCode(),
                            LocalDateTime.now())
                    );
                }
            } catch (Exception ex) {
                // 4. On any exception, flag this order as SR_AWB_GENERATION_FAILED
                o.setFulfillmentStatus(OrderFulfillmentStatus.SR_AWB_GENERATION_FAILED);
                o.getOrderFulfillmentStatusHistory().add(
                    new OrderFulfillmentStatusHistory(o,
                        OrderFulfillmentStatus.SR_AWB_GENERATION_FAILED,
                        "AWB generation failed with exception: " + ex.getMessage(),
                        LocalDateTime.now())
                );
                o.setErrorCode("SR_AWB_GENERATION_FAILED");
                o.setErrorMessage(ex.getMessage());
                log.error("Auto‐generate AWB failed for order {}: {}", o.getId(), ex.getMessage());
            }
        }

        // 5. Save all changes in batch
        orderRepo.saveAll(toProcess);

        // 6. Return updated DTOs
        return toProcess.stream()
                        .map(mapper::toOrderDTO)
                        .collect(Collectors.toList());
    }


    /**
     * 7) Schedule pickup for a single order. If Shiprocket reports pickup_status != 1,
     * or if any exception is thrown, mark order as SR_PICKUP_REQUEST_FAILED.
     * Otherwise set status = READY_TO_SHIP.
     */
    @Transactional
    public OrderDTO schedulePickup(Long orderId, PickupRequest req) {
        Order o = findOrder(orderId);
        try {
            // 1. Call Shiprocket’s generate/pickup endpoint
            PickupAssignWrapper pickupWrapper = courierService.requestPickup(
                o.getShiprocketShipmentId(),
                req.pickupDate()
            );

            // 2. If pickup_status == 1 → success, else → failure
            if (pickupWrapper.getPickupStatus() != null &&
                pickupWrapper.getPickupStatus().equals(1)) {
                o.setFulfillmentStatus(OrderFulfillmentStatus.READY_TO_SHIP);
                o.getOrderFulfillmentStatusHistory().add(
                    new OrderFulfillmentStatusHistory(o,
                        OrderFulfillmentStatus.READY_TO_SHIP,
                        "Pickup scheduled successfully for " + req.pickupDate(),
                        LocalDateTime.now())
                );

                // Optionally store the actual scheduled date string:
                o.setPickupScheduled(
                    pickupWrapper.getResponse().getPickupScheduledDate()
                );
            } else {
                // Shiprocket returned pickup_status != 1
                o.setFulfillmentStatus(OrderFulfillmentStatus.SR_PICKUP_REQUEST_FAILED);
                o.getOrderFulfillmentStatusHistory().add(
                    new OrderFulfillmentStatusHistory(o,
                        OrderFulfillmentStatus.SR_PICKUP_REQUEST_FAILED,
                        "Pickup request failed with status: " + pickupWrapper.getPickupStatus(),
                        LocalDateTime.now())
                );
                o.setErrorCode("SR_PICKUP_REQUEST_FAILED");
                o.setErrorMessage(
                    "Pickup call returned status=" + pickupWrapper.getPickupStatus()
                );
            }
        } catch (Exception ex) {
            // Any exception during the call → mark as failure
            o.setFulfillmentStatus(OrderFulfillmentStatus.SR_PICKUP_REQUEST_FAILED);
            o.getOrderFulfillmentStatusHistory().add(
                new OrderFulfillmentStatusHistory(o,
                    OrderFulfillmentStatus.SR_PICKUP_REQUEST_FAILED,
                    "Pickup request failed with exception: " + ex.getMessage(),
                    LocalDateTime.now())
            );
            o.setErrorCode("SR_PICKUP_REQUEST_FAILED");
            o.setErrorMessage(ex.getMessage());
            log.error("Pickup request failed for order {}: {}", orderId, ex.getMessage());
        }

        orderRepo.save(o);
        return mapper.toOrderDTO(o);
    }

     /**
     * 8) AUTO‐REQUEST PICKUP for every order in AWB_GENERATED status.
     *     - On success → set status = READY_TO_SHIP.
     *     - On failure → set status = SR_PICKUP_REQUEST_FAILED.
     */
    @Transactional
    public List<OrderDTO> autoRequestPickupForAllAwbGenerated(String pickupDate) {
        // 1. Fetch all orders with status == AWB_GENERATED
        List<Order> toProcess = orderRepo.findByFulfillmentStatus(OrderFulfillmentStatus.AWB_GENERATED);

        for (Order o : toProcess) {
            try {
                // 2. Call Shiprocket to generate/confirm pickup
                PickupAssignWrapper pickupWrapper = courierService.requestPickup(
                    o.getShiprocketShipmentId(),
                    pickupDate
                );

                if (pickupWrapper.getPickupStatus() != null &&
                    pickupWrapper.getPickupStatus().equals(1)) {
                    o.setFulfillmentStatus(OrderFulfillmentStatus.READY_TO_SHIP);
                    o.getOrderFulfillmentStatusHistory().add(
                        new OrderFulfillmentStatusHistory(o,
                            OrderFulfillmentStatus.READY_TO_SHIP,
                            "Pickup scheduled successfully for " + pickupDate,
                            LocalDateTime.now())
                    );
                    o.setPickupScheduled(
                        pickupWrapper.getResponse().getPickupScheduledDate()
                    );
                } else {
                    o.setFulfillmentStatus(OrderFulfillmentStatus.SR_PICKUP_REQUEST_FAILED);
                    o.getOrderFulfillmentStatusHistory().add(
                        new OrderFulfillmentStatusHistory(o,
                            OrderFulfillmentStatus.SR_PICKUP_REQUEST_FAILED,
                            "Pickup request failed with status: " + pickupWrapper.getPickupStatus(),
                            LocalDateTime.now())
                    );
                    o.setErrorCode("SR_PICKUP_REQUEST_FAILED");
                    o.setErrorMessage(
                        "Pickup call returned status=" + pickupWrapper.getPickupStatus()
                    );
                }
            } catch (Exception ex) {
                o.setFulfillmentStatus(OrderFulfillmentStatus.SR_PICKUP_REQUEST_FAILED);
                o.getOrderFulfillmentStatusHistory().add(
                    new OrderFulfillmentStatusHistory(o,
                        OrderFulfillmentStatus.SR_PICKUP_REQUEST_FAILED,
                        "Pickup request failed with exception: " + ex.getMessage(),
                        LocalDateTime.now())
                );
                o.setErrorCode("SR_PICKUP_REQUEST_FAILED");
                o.setErrorMessage(ex.getMessage());
                log.error("Auto‐pickup request failed for order {}: {}", o.getId(), ex.getMessage());
            }
        }

        // 3. Save all changes in batch
        orderRepo.saveAll(toProcess);

        // 4. Return updated DTOs
        return toProcess.stream()
                        .map(mapper::toOrderDTO)
                        .collect(Collectors.toList());
    }

     /**
     * 9) Generate labels (unchanged)
     */
    @Transactional
    public OrderDTO generateLabels(Long orderId) {
        Order o = findOrder(orderId);
        LabelResponse label = lmiService.generateLabel(
                List.of(o.getShiprocketShipmentId()));

        o.setShippingLabelUrl(label.getLabelUrl()); // use your POJO’s field
        orderRepo.save(o);
        return mapper.toOrderDTO(o);
    }

     /**
     * 10) Generate manifest (unchanged)
     */
    @Transactional
    public OrderDTO generateManifest(Long orderId) {
        Order o = findOrder(orderId);
        ManifestResponse manifest = lmiService.generateManifest(
                List.of(o.getShiprocketShipmentId()));
        o.setManifestUrl(manifest.getManifestUrl());
        orderRepo.save(o);
        return mapper.toOrderDTO(o);
    }

    /**
    * 11) List orders by arbitrary status (unchanged)
    */
    @Transactional
    public List<OrderDTO> listByFulfillmentStatus(OrderFulfillmentStatus status) {
        return orderRepo.findByFulfillmentStatus(status).stream()
                .map(mapper::toOrderDTO)
                .collect(Collectors.toList());
    }

    /**
     * 12) Create Shiprocket order manually if SHIPMENT_FAILED (unchanged)
     */
    @Transactional
    public OrderDTO createShiprocketOrder(Long orderId) {
        Order order = findOrder(orderId);
        if (order.getFulfillmentStatus() != OrderFulfillmentStatus.SHIPROCKET_ORDER_CREATION_FAILED) {
            throw new IllegalStateException("Order is not in SHIPROCKET_ORDER_CREATION_FAILED state");
        }

        try {
            CreateOrderResponse srResp = shiprocketOrderService.createOrder(order);
            order.setShiprocketOrderId(srResp.getOrderId());
            order.setShiprocketShipmentId(srResp.getShipmentId());
            order.setFulfillmentStatus(OrderFulfillmentStatus.PROCESSING);
            order.getOrderFulfillmentStatusHistory().add(
                new OrderFulfillmentStatusHistory(order,
                    OrderFulfillmentStatus.PROCESSING,
                    "Manually created Shiprocket order by admin with ID: " + srResp.getOrderId(),
                    LocalDateTime.now())
            );
            order.setErrorCode(null); // Clear previous error
            order.setErrorMessage(null);
        } catch (Exception ex) {
            log.error("Failed to manually create Shiprocket order for order {}: {}", orderId, ex.getMessage());
            order.setErrorCode("SHIPROCKET_ORDER_CREATION_FAILED");
            order.setErrorMessage(ex.getMessage());
            order.setFulfillmentStatus(OrderFulfillmentStatus.SHIPROCKET_ORDER_CREATION_FAILED);
            order.getOrderFulfillmentStatusHistory().add(
                new OrderFulfillmentStatusHistory(order,
                    OrderFulfillmentStatus.SHIPROCKET_ORDER_CREATION_FAILED,
                    "Failed to create manually Shiprocket order: " + ex.getMessage(),
                    LocalDateTime.now())
            );
            orderRepo.save(order);
            throw new ShiprocketApiException("Failed to create Shiprocket order: " + ex.getMessage());
        }

        orderRepo.save(order);
        return mapper.toOrderDTO(order);
    }

     /** 13) List all CANCELLED orders that still need a refund */
    @Transactional(readOnly = true)
    public List<OrderDTO> listCancelledAwaitingRefund() {
        return orderRepo.findAll().stream()
            .filter(o ->
                o.getFulfillmentStatus() == OrderFulfillmentStatus.CANCELLED
                && o.getOrderPaymentStatus() == OrderPaymentStatus.REFUND_PENDING
            )
            .map(mapper::toOrderDTO)
            .toList();
    }

     /** 14) Process the refund */
    @Transactional
    public OrderDTO refundOrder(Long orderId) {
        Order o = orderRepo.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (o.getOrderPaymentStatus() != OrderPaymentStatus.REFUND_PENDING) {
            throw new IllegalStateException("Order is not awaiting refund");
        }

        try {
            // attempt the refund
            paymentService.refundPayment(orderId);

            // on success
            o.setOrderPaymentStatus(OrderPaymentStatus.REFUNDED);
            o.getOrderPaymentStatusHistory().add(new OrderPaymentStatusHistory(
                o,
                OrderPaymentStatus.REFUNDED,
                "Refund processed by admin",
                LocalDateTime.now()
            ));

        } catch (RazorpayException ex) {
            log.error("Refund failed for order {}: {}", orderId, ex.getMessage());

            // mark the refund as failed
            o.setOrderPaymentStatus(OrderPaymentStatus.REFUND_FAILED);
            o.getOrderPaymentStatusHistory().add(new OrderPaymentStatusHistory(
                o,
                OrderPaymentStatus.REFUND_FAILED,
                "Refund attempt failed: " + ex.getMessage(),
                LocalDateTime.now()
            ));

            orderRepo.save(o);
            // rethrow or wrap in your own exception type
            throw new ShiprocketApiException("Refund failed for order " + orderId + ": " + ex.getMessage(), ex);
        }

        orderRepo.save(o);
        return mapper.toOrderDTO(o);
    }


    // ───helpers────────

    private Order findOrder(Long id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private Integer optionalInt(Double d) {
        return (d == null) ? null : d.intValue();
    }

}
