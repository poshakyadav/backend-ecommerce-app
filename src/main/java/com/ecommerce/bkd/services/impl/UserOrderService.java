package com.ecommerce.bkd.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.apache.coyote.BadRequestException;
import org.json.JSONObject;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.ecommerce.bkd.dtos.payment.PaymentInitiateRequest;
import com.ecommerce.bkd.dtos.shiprocket.CourierServiceability;
import com.ecommerce.bkd.dtos.shiprocket.CreateOrderResponse;
import com.ecommerce.bkd.dtos.shiprocket.ServiceabilityData;
import com.ecommerce.bkd.dtos.shiprocket.ShiprocketResponse;
import com.ecommerce.bkd.exceptions.ResourceNotFoundException;
import com.ecommerce.bkd.exceptions.ShiprocketApiException;
import com.ecommerce.bkd.models.Cart;
import com.ecommerce.bkd.models.CartItem;
import com.ecommerce.bkd.models.Order;
import com.ecommerce.bkd.models.OrderFulfillmentStatusHistory;
import com.ecommerce.bkd.models.OrderItem;
import com.ecommerce.bkd.models.OrderPaymentStatusHistory;
import com.ecommerce.bkd.models.ProductVariant;
import com.ecommerce.bkd.models.SizePrice;
import com.ecommerce.bkd.models.User;
import com.ecommerce.bkd.models.UserAddress;
import com.ecommerce.bkd.models.enums.OrderFulfillmentStatus;
import com.ecommerce.bkd.models.enums.OrderPaymentStatus;
import com.ecommerce.bkd.models.enums.PaymentMode;
import com.ecommerce.bkd.records.CreateOrderRequest;
import com.ecommerce.bkd.records.OrderDTO;
import com.ecommerce.bkd.records.order.CreateDirectOrderRequest;
import com.ecommerce.bkd.repositories.CartRepository;
import com.ecommerce.bkd.repositories.OrderRepository;
import com.ecommerce.bkd.repositories.ProductVariantRepository;
import com.ecommerce.bkd.repositories.UserRepository;
import com.ecommerce.bkd.services.CartService;
import com.ecommerce.bkd.services.OrderMapper;
import com.ecommerce.bkd.services.PaymentService;
import com.razorpay.RazorpayException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserOrderService {

    private final CartRepository cartRepository;
    private final CartService cartService;
    private final ProductVariantRepository variantRepository;
    private final PaymentService paymentService;
    private final OrderRepository orderRepo;
    private final OrderMapper mapper;
    private final ShiprocketCourierService courierService;
    private final ShiprocketOrderService shiprocketOrderService;
    private final UserRepository userRepository;

    @Transactional
    public OrderDTO createOrder(Long userId, CreateOrderRequest req) throws BadRequestException, RazorpayException {

        // Fetch the cart & validate
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Cart not found"));
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

       // Stock validation
        for (CartItem item : cart.getItems()) {
            ProductVariant variant = item.getVariant();
            String selectedSize = item.getSelectedSize();
            SizePrice sizePrice = variant.getSizePrices().stream()
                    .filter(sp -> sp.getSize().equals(selectedSize))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Selected size not found"));
            if (sizePrice.getStock() < item.getQuantity()) {
                throw new BadRequestException("Insufficient stock for size " + selectedSize + " of variant: " + variant.getSku());
            }
        }

        // serviceability check
        ShiprocketResponse<ServiceabilityData> resp = courierService
            .checkServiceability(
                        mapper.getPickupPincode(),
                        mapper.getUserPincode(req.userbillingAddressId()),
                        mapper.getCartWeight(cart),
                        mapper.getPaymentMode(req.paymentMode())
            );

        List<CourierServiceability> opts = resp
            .getData()                        // → ServiceabilityData
            .getAvailableCourierCompanies();  // → List<CourierServiceability>
            // fail if no couriers returned
            
        if (opts.isEmpty()) {
            throw new BadRequestException("Address not serviceable");
        }

        /**
         * Map to Order entity (this already sets paymentStatus=PAYMENT_PENDING, fulfillmentStatus=CREATED)
         * & calculate totals
         */
        Order order = mapper.toOrderEntity(userId, cart, req);
        order.setFromCart(true); // Cart-based order
        // Compute all order-level totals
        populateOrderTotals(order);

        // Set initial status
        order.setOrderPaymentStatus(OrderPaymentStatus.PAYMENT_PENDING);
        order.setFulfillmentStatus(OrderFulfillmentStatus.CREATED);
        order.setPaymentStatus("PENDING");

        // Set status to PAYMENT_PENDING for both modes; we’ll branch next
        // order.setStatus(OrderStatus.PAYMENT_PENDING);
        // order.setPaymentMode(req.paymentMode());

         // If COD, we want to remember that
        if (PaymentMode.COD.equals(req.paymentMode())) {
            order.setCodOrder(true);
            // Mark paymentStatus so that you know this is waiting on courier collection
            order.setPaymentStatus("PENDING_COD");
            // record fulfillment transition if needed
        }

        log.info("Creating order for user {} with request {}", userId, req);
        orderRepo.save(order);
        log.info("Order {} created (mode={})", order.getId(), req.paymentMode());
        
        // 5. Branch on payment mode
        if (PaymentMode.PREPAID.equals(req.paymentMode())) {
            // ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
            // 5a. PREPAID flow: initiate Razorpay → wait for webhook → then Shiprocket
            PaymentInitiateRequest optsReq = new PaymentInitiateRequest(
                order.getId(),
                order.getTotalOrderPrice(),
                req.currency()
            );
            JSONObject razorOrder = paymentService.initiatePayment(optsReq);

            // We return a DTO that contains Razorpay order info (amount, id, etc)
            return mapper.toOrderDTO(order, razorOrder);
        } else {
             // ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
            // 5b. COD flow: no Razorpay call, immediately reduce stock, clear cart, call Shiprocket.
            // (1) Reduce stock right away
            for (OrderItem item : order.getItems()) {
                ProductVariant variant = item.getVariant();
                SizePrice sizePrice = variant.getSizePrices().stream()
                        .filter(sp -> sp.getSize().equals(item.getSelectedSize()))
                        .findFirst()
                        .orElseThrow();
                int newStock = sizePrice.getStock() - item.getQuantity();
                if (newStock < 0) {
                    throw new IllegalStateException("Stock cannot be negative for size " + item.getSelectedSize());
                }
                sizePrice.setStock(newStock);
                variantRepository.save(variant);
            }
            // (2) Clear the cart
            cartService.clearCart(userId);

            // (3) Create in Shiprocket with retry
            try {
                createShiprocketOrderWithRetry(order);
                // Now that Shiprocket order is created successfully:
                order.setFulfillmentStatus(OrderFulfillmentStatus.PROCESSING);
                order.getOrderFulfillmentStatusHistory().add(
                new OrderFulfillmentStatusHistory(order,
                    OrderFulfillmentStatus.PROCESSING,
                    "Shiprocket order created for COD",
                    LocalDateTime.now())
                );

            } catch (Exception ex) {
                log.error(
                    "Failed to create Shiprocket order for COD order {}: {}",
                    order.getId(),
                    ex.getMessage()
                );
                // order.setStatus(OrderStatus.SHIPMENT_FAILED);
                // Mark fulfillment failure:
                order.setFulfillmentStatus(OrderFulfillmentStatus.SHIPROCKET_ORDER_CREATION_FAILED);
                order.getOrderFulfillmentStatusHistory().add(
                new OrderFulfillmentStatusHistory(order,
                    OrderFulfillmentStatus.SHIPROCKET_ORDER_CREATION_FAILED,
                    "Shiprocket order creation failed: " + ex.getMessage(),
                    LocalDateTime.now())
                );
                order.setErrorCode("SHIPROCKET_ORDER_CREATION_FAILED");
                order.setErrorMessage(ex.getMessage());
                // Optionally notify admins…
            }

            orderRepo.save(order);

            // Return a DTO that has no Razorpay fields (you can adapt mapper)
            return mapper.toOrderDTO(order);
        }
    }

    @Transactional
    public OrderDTO createDirectOrder(Long userId, CreateDirectOrderRequest req) throws BadRequestException, RazorpayException {
        // Step 1: Validate inputs
        if (req.quantity() <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }

        ProductVariant variant = variantRepository.findById(req.variantId())
                .orElseThrow(() -> new BadRequestException("Variant not found: " + req.variantId()));
        if (!variant.isActive()) {
            throw new BadRequestException("Variant is inactive: " + req.variantId());
        }

        SizePrice sizePrice = variant.getSizePrices().stream()
                .filter(sp -> sp.getSize().equals(req.selectedSize()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Size not found: " + req.selectedSize()));
        if (sizePrice.getStock() < req.quantity()) {
            throw new BadRequestException("Insufficient stock for size " + req.selectedSize() + ": " + sizePrice.getStock());
        }

        // Step 2: Check serviceability
        double orderWeight = variant.getWeight().doubleValue() * req.quantity();
        ShiprocketResponse<ServiceabilityData> serviceabilityResp = courierService.checkServiceability(
                mapper.getPickupPincode(),
                mapper.getUserPincode(req.userbillingAddressId()),
                orderWeight,
                mapper.getPaymentMode(req.paymentMode())
        );
        List<CourierServiceability> couriers = serviceabilityResp.getData().getAvailableCourierCompanies();
        if (couriers.isEmpty()) {
            throw new BadRequestException("Address not serviceable for pincode: " + mapper.getUserPincode(req.userbillingAddressId()));
        }

        // Step 3: Create Order entity
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found: " + userId));
        UserAddress billingAddress = user.getUserAddresses().stream()
                .filter(addr -> addr.getId().equals(req.userbillingAddressId()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Billing address not found: " + req.userbillingAddressId()));

        Order order = new Order();
        order.setUser(user);
        order.setFromCart(false); // Direct "Buy Now" order
        order.setOrderPaymentStatus(OrderPaymentStatus.PAYMENT_PENDING);
        order.setFulfillmentStatus(OrderFulfillmentStatus.CREATED);
        order.setPaymentStatus("PENDING");
        order.setPaymentMode(req.paymentMode());
        order.setCurrency(req.currency());
        order.setContactPhone(req.contactPhone());
        order.setAlternativePhone(req.alternativePhone());
        order.setOrderNotes(req.deliveryInstructions());
        order.setBillingAddressAndCreateSnapshot(billingAddress);

        // Set package dimensions
        order.setPackageLength(variant.getLength().doubleValue());
        order.setPackageBreadth(variant.getBreadth().doubleValue());
        order.setPackageHeight(variant.getHeight().doubleValue());
        order.setPackageWeight(orderWeight);

        // Create OrderItem
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setVariant(variant);
        orderItem.setQuantity(req.quantity());
        orderItem.setSelectedSize(req.selectedSize());
        orderItem.setBasePrice(sizePrice.getBasePrice());
        orderItem.setDiscount(sizePrice.getDiscount() != null ? sizePrice.getDiscount() : BigDecimal.ZERO);
        orderItem.setTaxRate(sizePrice.getTaxRate() != null ? sizePrice.getTaxRate() : BigDecimal.ZERO);
        orderItem.setCreatedAt(LocalDateTime.now()); // Ensure createdAt is set
        order.setItems(List.of(orderItem));

        // Calculate totals
        populateOrderTotals(order);

        // Save initial order state
        orderRepo.save(order);
        log.info("Direct order created for user {}: orderId={}, mode={}", userId, order.getId(), req.paymentMode());

        // Step 4: Handle payment mode
        if (PaymentMode.PREPAID.equals(req.paymentMode())) {
            // Prepaid flow: Initiate Razorpay payment
            PaymentInitiateRequest paymentReq = new PaymentInitiateRequest(
                    order.getId(),
                    order.getTotalOrderPrice(),
                    req.currency()
            );
            JSONObject razorOrder = paymentService.initiatePayment(paymentReq);
            order.setRazorpayOrderId(razorOrder.getString("id"));
            orderRepo.save(order);
            log.info("Razorpay payment initiated for order {}: razorpayOrderId={}", order.getId(), order.getRazorpayOrderId());
            return mapper.toOrderDTO(order, razorOrder);
        } else if (PaymentMode.COD.equals(req.paymentMode())) {
            // COD flow: Process immediately
            order.setCodOrder(true);
            order.setPaymentStatus("PENDING_COD");
            order.getOrderPaymentStatusHistory().add(
                    new OrderPaymentStatusHistory(order, OrderPaymentStatus.PAYMENT_PENDING, "COD order created", LocalDateTime.now())
            );

            // Reduce stock immediately
            int newStock = sizePrice.getStock() - req.quantity();
            if (newStock < 0) {
                throw new IllegalStateException("Stock cannot be negative for size " + req.selectedSize());
            }
            sizePrice.setStock(newStock);
            variantRepository.save(variant);
            log.info("Stock reduced for variant {} size {}: newStock={}", variant.getId(), req.selectedSize(), newStock);

            // Create Shiprocket order
            try {
                CreateOrderResponse shiprocketResp = createShiprocketOrderWithRetry(order);
                order.setShiprocketOrderId(shiprocketResp.getOrderId());
                order.setShiprocketShipmentId(shiprocketResp.getShipmentId());

                order.setFulfillmentStatus(OrderFulfillmentStatus.PROCESSING);
                order.getOrderFulfillmentStatusHistory().add(
                        new OrderFulfillmentStatusHistory(order, OrderFulfillmentStatus.PROCESSING, "Shiprocket order created for COD", LocalDateTime.now())
                );
                log.info("Shiprocket order created for COD order {}: shiprocketOrderId={}", order.getId(), shiprocketResp.getOrderId());
            } catch (Exception ex) {
                log.error("Failed to create Shiprocket order for COD order {}: {}", order.getId(), ex.getMessage());
                order.setFulfillmentStatus(OrderFulfillmentStatus.SHIPROCKET_ORDER_CREATION_FAILED);
                order.getOrderFulfillmentStatusHistory().add(
                        new OrderFulfillmentStatusHistory(order, OrderFulfillmentStatus.SHIPROCKET_ORDER_CREATION_FAILED, "Shiprocket failure: " + ex.getMessage(), LocalDateTime.now())
                );
                order.setErrorCode("SHIPROCKET_ORDER_CREATION_FAILED");
                order.setErrorMessage(ex.getMessage());
                orderRepo.save(order);
                throw new BadRequestException("Failed to process COD order due to shipping error: " + ex.getMessage());
            }

            orderRepo.save(order);
            return mapper.toOrderDTO(order);
        } else {
            throw new BadRequestException("Unsupported payment mode: " + req.paymentMode());
        }
    }


    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        Order o = findOwnedOrder(userId, orderId);

        // 1) ensure cancel before pickup
        if (EnumSet.of(
                OrderFulfillmentStatus.READY_TO_SHIP,
                OrderFulfillmentStatus.SHIPPED,
                OrderFulfillmentStatus.IN_TRANSIT,
                OrderFulfillmentStatus.OUT_FOR_DELIVERY,
                OrderFulfillmentStatus.DELIVERED
            ).contains(o.getFulfillmentStatus())) {
            throw new IllegalStateException("Too late to cancel");
        }

        // 2) call Shiprocket cancel if order was already created there
        if (o.getShiprocketOrderId() != null) {
            shiprocketOrderService.cancelOrder(o.getShiprocketOrderId());
        }

        // 3) update fulfillment status + history
        o.setFulfillmentStatus(OrderFulfillmentStatus.CANCELLED);
        o.getOrderFulfillmentStatusHistory().add(new OrderFulfillmentStatusHistory(
            o,
            OrderFulfillmentStatus.CANCELLED,
            "User cancelled before pickup",
            LocalDateTime.now()
        ));

        // 4) decide payment‐status on cancel
        if (o.isCodOrder()) {
            // no money to refund
            o.setOrderPaymentStatus(OrderPaymentStatus.NO_REFUND_REQUIRED);
            o.getOrderPaymentStatusHistory().add(new OrderPaymentStatusHistory(
                o,
                OrderPaymentStatus.NO_REFUND_REQUIRED,
                "COD cancel — no refund needed",
                LocalDateTime.now()
            ));
        } else {
            // prepaid → refund will happen later
            o.setOrderPaymentStatus(OrderPaymentStatus.REFUND_PENDING);
            o.getOrderPaymentStatusHistory().add(new OrderPaymentStatusHistory(
                o,
                OrderPaymentStatus.REFUND_PENDING,
                "Prepaid cancel — refund pending",
                LocalDateTime.now()
            ));
        }

        // Restore stock
        for (OrderItem item : o.getItems()) {
            ProductVariant variant = item.getVariant();
            SizePrice sizePrice = variant.getSizePrices().stream()
                    .filter(sp -> sp.getSize().equals(item.getSelectedSize()))
                    .findFirst()
                    .orElseThrow();
            sizePrice.setStock(sizePrice.getStock() + item.getQuantity());
            variantRepository.save(variant);
        }

        orderRepo.save(o);
    }

    public List<OrderDTO> listOrders(Long userId) {
        return orderRepo.findByUserId(userId).stream()
                .map(mapper::toOrderDTO)
                .toList();
    }

    public OrderDTO getOrder(Long userId, Long orderId) {
        Order order = findOwnedOrder(userId, orderId);
        return mapper.toOrderDTO(order);
    }

    // ─── helpers ────

    private Order findOwnedOrder(Long userId, Long orderId) {
        return orderRepo.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    /**
     * Populates all pricing-related totals in the Order entity based on its OrderItems.
     * Calculates base price, discount, discounted price, tax, and final order price.
     *
     * @param order The Order entity to populate with totals.
     */
    private void populateOrderTotals(Order order) {
        // Initialize accumulators for base price, discount, discounted price, and tax
        BigDecimal baseSum = BigDecimal.ZERO;  // Sum of base
        BigDecimal discountSum = BigDecimal.ZERO; // Sum of discount
        BigDecimal discountedPriceSum = BigDecimal.ZERO; // Sum of discounted prices (pre-tax)
        BigDecimal taxSum = BigDecimal.ZERO; // Sum of tax amounts

        // Aggregate totals from each OrderItem
        for (OrderItem item : order.getItems()) {
            // Use pre-calculated values from CartItem, which already include quantity
            baseSum = baseSum.add(item.getBasePrice()); // Total base price (qty included)
            discountSum = discountSum.add(item.getDiscountAmount()); // Total discount amount 
            discountedPriceSum = discountedPriceSum.add(item.getDiscountedPrice()); // Total discounted price
            taxSum = taxSum.add(item.getTaxAmount()); // Total tax amount
        }

        // Set total base price before discounts
        order.setTotalBasePrice(baseSum);

        // Set total discount amount applied to the order
        order.setTotalDiscountAmount(discountSum);

        // Calculate and set the overall discount rate (discountSum / baseSum)
        // If baseSum is zero, set to zero to avoid division by zero
        order.setTotalDiscount(
            baseSum.compareTo(BigDecimal.ZERO) > 0
                ? discountSum.divide(baseSum, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO
        );

        // Set total discounted price (sum of all items' discounted prices)
        order.setTotalDiscountedPrice(discountedPriceSum);

        // Set total tax amount for the order
        order.setTotalTaxAmount(taxSum);

        // Calculate and set the effective tax rate (taxSum / discountedPriceSum)
        // If discountedPriceSum is zero, set to zero to avoid division by zero
        order.setTotalTaxRate(
            discountedPriceSum.compareTo(BigDecimal.ZERO) > 0
                ? taxSum.divide(discountedPriceSum, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO
        );

        // Use shipping cost if provided, otherwise default to zero
        BigDecimal shipping = Optional.ofNullable(order.getShippingCost()).orElse(BigDecimal.ZERO);

        // Calculate and set the final order price: discountedPriceSum + taxSum + shipping
        order.setTotalOrderPrice(discountedPriceSum.add(taxSum).add(shipping));
    }


    /**  
     * Helper method for RazorpayWebhookController
    */
    @Transactional
    public void processPaymentSuccess(Order order, String paymentId) {
        // Update order details
        order.setRazorpayPaymentId(paymentId);
        order.setPaymentStatus("PAID");
        order.setOrderPaymentStatus(OrderPaymentStatus.PAYMENT_SUCCESS);
        order.setPaymentDate(LocalDateTime.now());
        order.getOrderPaymentStatusHistory().add(
            new OrderPaymentStatusHistory(order,
                OrderPaymentStatus.PAYMENT_SUCCESS,
                "Payment captured via webhook",
                LocalDateTime.now())
        );
        log.info("Payment success for order {}: paymentId={}", order.getId(), paymentId);
        
        // Reduce stock for each product in the order
        for (OrderItem item : order.getItems()) {
            ProductVariant variant = item.getVariant();
            SizePrice sizePrice = variant.getSizePrices().stream()
                    .filter(sp -> sp.getSize().equals(item.getSelectedSize()))
                    .findFirst()
                    .orElseThrow();
            int newStock = sizePrice.getStock() - item.getQuantity();
            if (newStock < 0) {
                throw new IllegalStateException("Stock cannot be negative for size " + item.getSelectedSize());
            }
            sizePrice.setStock(newStock);
            variantRepository.save(variant);
            log.info("Stock reduced for variant {} size {}: newStock={}", variant.getId(), item.getSelectedSize(), newStock);
        }
                
        // Clear cart only for cart-based orders
        if (order.isFromCart()) {
            cartService.clearCart(order.getUser().getId());
            log.info("Cart cleared for user {} after order {}", order.getUser().getId(), order.getId());
        }

        try {
            CreateOrderResponse shiprocketResp = createShiprocketOrderWithRetry(order);
            order.setShiprocketOrderId(shiprocketResp.getOrderId());
            order.setShiprocketShipmentId(shiprocketResp.getShipmentId());

            order.setFulfillmentStatus(OrderFulfillmentStatus.PROCESSING);
            order.getOrderFulfillmentStatusHistory().add(
                new OrderFulfillmentStatusHistory(order,
                    OrderFulfillmentStatus.PROCESSING,
                    "Shiprocket order created",
                    LocalDateTime.now())
            );
            log.info("Shiprocket order created for order {}: shiprocketOrderId={}", order.getId(), shiprocketResp.getOrderId());
        } catch (Exception ex) {
            log.error("Failed to create Shiprocket order for order {} after retries: {}", order.getId(), ex.getMessage());
            order.setFulfillmentStatus(OrderFulfillmentStatus.SHIPROCKET_ORDER_CREATION_FAILED); // Flag for manual review OrderStatus.MANUAL_REVIEW
            order.getOrderFulfillmentStatusHistory().add(
                new OrderFulfillmentStatusHistory(order,
                    OrderFulfillmentStatus.SHIPROCKET_ORDER_CREATION_FAILED,
                    ex.getMessage(),
                    LocalDateTime.now())
            );
            order.setErrorCode("SHIPROCKET_ORDER_CREATION_FAILED");
            order.setErrorMessage(ex.getMessage());
            // Optionally, notify admins (e.g., via email or queue)
        }
        
        orderRepo.save(order);
        
    }

    @Retryable(value = ShiprocketApiException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    private CreateOrderResponse createShiprocketOrderWithRetry(Order order) {
        log.info("Attempting to create Shiprocket order for orderId={}", order.getId());
        CreateOrderResponse response = shiprocketOrderService.createOrder(order);
        return response;
    }
}
