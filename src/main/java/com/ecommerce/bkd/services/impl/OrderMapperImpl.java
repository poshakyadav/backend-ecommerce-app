package com.ecommerce.bkd.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.ecommerce.bkd.dtos.shiprocket.ShiprocketOrder;
import com.ecommerce.bkd.models.Cart;
import com.ecommerce.bkd.models.CartItem;
import com.ecommerce.bkd.models.Order;
import com.ecommerce.bkd.models.OrderFulfillmentStatusHistory;
import com.ecommerce.bkd.models.OrderItem;
import com.ecommerce.bkd.models.OrderPaymentStatusHistory;
import com.ecommerce.bkd.models.PickupLocation;
import com.ecommerce.bkd.models.ProductVariant;
import com.ecommerce.bkd.models.User;
import com.ecommerce.bkd.models.UserAddress;
import com.ecommerce.bkd.models.enums.OrderFulfillmentStatus;
import com.ecommerce.bkd.models.enums.OrderPaymentStatus;
import com.ecommerce.bkd.models.enums.PaymentMode;
import com.ecommerce.bkd.records.CreateOrderRequest;
import com.ecommerce.bkd.records.OrderDTO;
import com.ecommerce.bkd.records.OrderItemDTO;
import com.ecommerce.bkd.repositories.PickupLocationRepository;
import com.ecommerce.bkd.repositories.UserAddressRepository;
import com.ecommerce.bkd.repositories.UserRepository;
import com.ecommerce.bkd.services.OrderMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderMapperImpl implements OrderMapper {
        private final UserRepository userRepo;
        private final UserAddressRepository addressRepo;
        private final PickupLocationRepository pickupRepo;

        @Override
        public Order toOrderEntity(Long userId, Cart cart, CreateOrderRequest req) {
                User user = userRepo.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                UserAddress billing = addressRepo.findById(req.userbillingAddressId())
                                .orElseThrow(() -> new IllegalArgumentException("Billing address not found"));

                Order order = new Order();
                order.setUser(user);

                // initial statuses
                order.setOrderPaymentStatus(OrderPaymentStatus.PAYMENT_PENDING);
                order.getOrderPaymentStatusHistory().add(
                new OrderPaymentStatusHistory(order,
                OrderPaymentStatus.PAYMENT_PENDING,
                "Initial status",
                LocalDateTime.now())
                );

                order.setFulfillmentStatus(OrderFulfillmentStatus.CREATED);
                order.getOrderFulfillmentStatusHistory().add(
                new OrderFulfillmentStatusHistory(
                        order,
                        OrderFulfillmentStatus.CREATED,
                        "Order created",
                        LocalDateTime.now()
                )
        );

                order.setPaymentMode(req.paymentMode());
                order.setCurrency(req.currency());
                order.setPaymentStatus("PENDING"); // string‐based payment field
                order.setContactPhone(req.contactPhone());
                order.setAlternativePhone(req.alternativePhone());
                order.setCreatedAt(LocalDateTime.now());
                order.setOrderNotes(req.deliveryInstructions());

                // Address snapshot
                order.setBillingAddressAndCreateSnapshot(billing);

                // Build items using CartItem values
                List<OrderItem> items = new ArrayList<>();
                for (CartItem ci : cart.getItems()) {
                        OrderItem oi = new OrderItem();
                        oi.setOrder(order);
                        oi.setVariant(ci.getVariant());
                        oi.setQuantity(ci.getQuantity());
                        oi.setSelectedSize(ci.getSelectedSize());

                        // Use pre-calculated values from CartItem
                        oi.setBasePrice(ci.getBasePrice());              // Total base price for quantity
                        oi.setDiscount(ci.getDiscount());                // Discount percentage
                        oi.setDiscountAmount(ci.getDiscountAmount());    // Total discount amount
                        oi.setDiscountedPrice(ci.getDiscountedPrice());  // Total discounted price
                        oi.setTaxRate(ci.getTaxRate());                  // Tax rate
                        oi.setTaxAmount(ci.getTaxAmount());              // Total tax amount
                        oi.setTotalPrice(ci.getTotalPrice());            // Total price (discountedPrice + taxAmount)
                        items.add(oi);
                    }
                order.setItems(items);

                // Leave shippingCost, orderNotes etc.
                // order.setShippingCost(req.shippingCost());

                // Calculate package dimensions
                calculatePackageDimensions(order);
                

                return order;
        }

        private void calculatePackageDimensions(Order order) {
                if (order.getItems().isEmpty()) {
                order.setPackageLength(0.0);
                order.setPackageBreadth(0.0);
                order.setPackageHeight(0.0);
                order.setPackageWeight(0.0);
                return;
                }

                double totalWeight = 0.0;
                double maxLength = 0.0;
                double maxBreadth = 0.0;
                double maxHeight = 0.0;

                for (OrderItem item : order.getItems()) {
                        ProductVariant variant = item.getVariant();
                        int quantity = item.getQuantity();
                        totalWeight += variant.getWeight().doubleValue() * quantity;
                        maxLength = Math.max(maxLength, variant.getLength().doubleValue());
                        maxBreadth = Math.max(maxBreadth, variant.getBreadth().doubleValue());
                        maxHeight = Math.max(maxHeight, variant.getHeight().doubleValue());
                }

                order.setPackageLength(maxLength);
                order.setPackageBreadth(maxBreadth);
                order.setPackageHeight(maxHeight);
                order.setPackageWeight(totalWeight);
        }

        @Override
        public OrderDTO toOrderDTO(Order order) {
                return toOrderDTO(order, (JSONObject) null);
        }

        @Override
        public OrderDTO toOrderDTO(Order order, JSONObject razorOrder) {
                BigDecimal total = Optional.ofNullable(order.getTotalOrderPrice()).orElse(BigDecimal.ZERO);
                String razorId = razorOrder != null ? razorOrder.optString("id") : null;
                String shipId = order.getShiprocketOrderId() != null ? order.getShiprocketOrderId().toString() : null;

                List<OrderItemDTO> items = order.getItems().stream()
                .map(i -> new OrderItemDTO(
                        i.getId(),
                        i.getVariant().getId(),
                        i.getVariant().getName(),
                        i.getQuantity(),
                        i.getBasePrice(),
                        i.getDiscountAmount(),
                        i.getTaxAmount(),
                        i.getDiscountedPrice(),
                        i.getSelectedSize(),
                        i.getVariant().getColor().getName(), // Use VariantColor
                        i.getTotalPrice()
                ))
                .collect(Collectors.toList());

                return new OrderDTO(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderPaymentStatus(),
                order.getFulfillmentStatus(),
                total,
                razorId,
                shipId,
                order.getShiprocketAwbNumber(),
                items,
                order.getPaymentMode(),
                order.getTotalTaxAmount(),
                order.getShippingCost(),
                order.getTotalDiscountAmount()
                );
        }

        
        @Override
        public OrderDTO toOrderDTO(Order order, ShiprocketOrder sr) {
                OrderDTO base = toOrderDTO(order, (JSONObject) null);
                // create a new record copying fields but overriding shiprocketOrderId
                return new OrderDTO(
                                base.id(),
                                base.orderNumber(),
                                base.paymentStatus(),
                                base.fulfillmentStatus(),
                                base.total(),
                                base.razorpayOrderId(),
                                sr.getOrderId().toString(),
                                base.shiprocketAwbNumber(),
                                base.items(),
                                base.paymentMode(),
                                base.tax(),
                                base.shippingCost(),
                                base.totalDiscount());
        }

        @Override
        public String getPickupPincode() {
                PickupLocation pl = pickupRepo.findByIsPrimaryLocationTrue()
                                .orElseThrow(() -> new IllegalStateException("No pickup location configured"));
                return pl.getPincode();
        }

        @Override
        public String getUserPincode(Long addressId) {
                return addressRepo.findById(addressId)
                                .map(UserAddress::getPincode)
                                .orElseThrow(() -> new IllegalArgumentException("Address not found"));
        }

        @Override
        public String getShippingPincode(Order o) {
                return o.getBillingAddressSnapshot().getPinCode();
        }

        @Override
        public double getCartWeight(Cart cart) {
                return cart.getItems().stream()
                        .mapToDouble(ci -> ci.getVariant().getWeight().doubleValue() * ci.getQuantity())
                        .sum();
        }

        @Override
        public boolean getPaymentMode(PaymentMode mode) {
                return mode == PaymentMode.COD ? true : false;
        }

}
