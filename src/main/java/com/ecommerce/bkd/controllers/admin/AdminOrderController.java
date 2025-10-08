package com.ecommerce.bkd.controllers.admin;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.bkd.dtos.shiprocket.BulkPickupRequest;
import com.ecommerce.bkd.dtos.shiprocket.CourierServiceability;
import com.ecommerce.bkd.models.enums.OrderFulfillmentStatus;
import com.ecommerce.bkd.records.AwbRequest;
import com.ecommerce.bkd.records.CourierSelectionRequest;
import com.ecommerce.bkd.records.OrderDTO;
import com.ecommerce.bkd.records.PickupRequest;
import com.ecommerce.bkd.services.impl.AdminOrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    /** 
     * 1) List all orders currently in PROCESSING (fulfillment‐status).
     */
    @GetMapping("/pending")
    public List<OrderDTO> listPending() {
        return adminOrderService.listByFulfillmentStatus(OrderFulfillmentStatus.PROCESSING);
    }

    /** 
     * 2) Accept a new order (move from PROCESSING → ACCEPTED)
     * Accept a single order (same as before)
     */
    @PostMapping("/{orderId}/accept")
    public OrderDTO accept(@PathVariable Long orderId) {
        return adminOrderService.acceptOrder(orderId);
    }

    /**
     * 3) AUTO‐ASSIGN all ACCEPTED orders in bulk.
     *    - Runs the “best‐courier” algorithm on each order.
     *    - Orders that succeed become COURIER_SELECTED.
     *    - Orders that fail serviceability become SERVICEABILITY_CHECK_FAILED.
     */
    @PostMapping("/auto-assign-couriers")
    public List<OrderDTO> autoAssignCouriers() {
        return adminOrderService.autoAssignCouriersForAllAccepted();
    }

     /**
     * 4) FETCH fresh serviceability options for one order (for “Override”).
     */
    @GetMapping("/{orderId}/available-couriers")
    public List<CourierServiceability> getAvailableCouriers(@PathVariable Long orderId) {
        return adminOrderService.fetchAvailableCouriers(orderId);
    }

    /**
     * 5) OVERRIDE courier selection on a single order.
     *    Expects { "courierId": 42 } in the request body.
     */
    @PostMapping("/{orderId}/select-courier")
    public OrderDTO overrideSelectCourier(
            @PathVariable Long orderId,
            @RequestBody CourierSelectionRequest req) {
        return adminOrderService.overrideSelectCourier(orderId, req);
    }

     /**
     * 6) Generate AWB for a given order (unchanged)
     */
    @PostMapping("/{orderId}/generate-awb")
    public OrderDTO generateAwb(@PathVariable Long orderId,
            @RequestBody AwbRequest req) {
        return adminOrderService.generateAwb(orderId, req);
    }

    /**
     * 7) AUTO‐GENERATE AWB for every order currently in COURIER_SELECTED status.
     *     - For each such order, calls Shiprocket’s generate AWB API.
     *     - On success → sets AWB fields and status = AWB_GENERATED.
     *     - On failure → sets status = SR_AWB_GENERATION_FAILED.
     */
    @PostMapping("/auto-generate-awb")
    public List<OrderDTO> autoGenerateAwb() {
        return adminOrderService.autoGenerateAwbForAllCourierSelected();
    }

     /**
     * 8) Schedule pickup for a single order (unchanged signature, but now uses enhanced service logic).
     */
    @PostMapping("/{orderId}/schedule-pickup")
    public OrderDTO schedulePickup(@PathVariable Long orderId,
            @RequestBody PickupRequest req) {
        return adminOrderService.schedulePickup(orderId, req);
    }

    /**
     * 9) AUTO‐REQUEST PICKUP for every order in AWB_GENERATED status.
     *     Expects a JSON body: { "pickupDate": "YYYY-MM-DD" }
     */
    @PostMapping("/auto-request-pickup")
    public List<OrderDTO> autoRequestPickup(@RequestBody BulkPickupRequest req) {
        return adminOrderService.autoRequestPickupForAllAwbGenerated(req.getPickupDate());
    }

     /**
     * 10) Generate labels (unchanged)
     */
    @PostMapping("/{orderId}/generate-labels")
    public OrderDTO generateLabels(@PathVariable Long orderId) {
        return adminOrderService.generateLabels(orderId);
    }

    /**
     * 11) Generate manifest (unchanged)
     */
    @PostMapping("/{orderId}/generate-manifest")
    public OrderDTO generateManifest(@PathVariable Long orderId) {
        return adminOrderService.generateManifest(orderId);
    }

    /** 
     * 12) This endpoint fetch list of that order that fail on to create 'Shiprocket Order' 
     * Need to change this endpoint path and write like -> sr-create-order--req-fail 
     */
    @GetMapping("/failed")
    public List<OrderDTO> listFailedSrOrderCreation() {
        return adminOrderService.listByFulfillmentStatus(OrderFulfillmentStatus.SHIPROCKET_ORDER_CREATION_FAILED);
    }

     /**
     * 13) Manually create a Shiprocket order for orders in SHIPROCKET_ORDER_CREATION_FAILED 
     */
    @PostMapping("/{orderId}/create-shiprocket-order")
    public OrderDTO createShiprocketOrder(@PathVariable Long orderId) {
        return adminOrderService.createShiprocketOrder(orderId);
    }

     /** 14) Fetch all user-cancelled but not yet refunded orders */
     @GetMapping("/cancelled-awaiting-refund")
     public List<OrderDTO> listCancelledAwaitingRefund() {
         return adminOrderService.listCancelledAwaitingRefund();
     }
 
     /** 15) Trigger refund for a single order */
     @PostMapping("/{orderId}/refund")
     public OrderDTO refundOrder(@PathVariable Long orderId) {
         return adminOrderService.refundOrder(orderId);
     }

    
}
