package com.ecommerce.bkd.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.ecommerce.bkd.models.enums.ExchangeOrderStatus;
import com.ecommerce.bkd.models.enums.OrderFulfillmentStatus;
import com.ecommerce.bkd.models.enums.OrderPaymentStatus;
import com.ecommerce.bkd.models.enums.OrderReturnStatus;
import com.ecommerce.bkd.models.enums.PaymentMode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The customer/user who placed the order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @NotNull
    @Column(nullable = false)
    private String contactPhone;

    private String alternativePhone;

    @NotNull
    @Column(nullable = false)
    private String currency;

    @JsonManagedReference
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    // the fromCart field to differentiate order sources
    @Column(name = "from_cart")
    private boolean fromCart = true;

    /**======================================================= */

    @Enumerated(EnumType.STRING)
    private OrderPaymentStatus orderPaymentStatus;

    @JsonManagedReference
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderPaymentStatusHistory> orderPaymentStatusHistory = new ArrayList<>();

    /*------------------------------------------------------- */

    @Enumerated(EnumType.STRING)
    private OrderFulfillmentStatus fulfillmentStatus;

    @JsonManagedReference
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderFulfillmentStatusHistory> orderFulfillmentStatusHistory = new ArrayList<>();

    /*------------------------------------------------------- */

    @Enumerated(EnumType.STRING)
    private OrderReturnStatus returnStatus;

    @JsonManagedReference
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderReturnStatusHistory> orderReturnStatusHistory = new ArrayList<>();

    /*------------------------------------------------------- */

    @Enumerated(EnumType.STRING)
    private ExchangeOrderStatus exchangeOrderStatus;

    @JsonManagedReference
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExchangeOrderStatusHistory> exchangeOrderStatusHistory  = new ArrayList<>();

    /**======================================================= */

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private UserAddress billingAddress;

    @JsonManagedReference
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private BillingAddressSnapshot billingAddressSnapshot;

    private String orderNotes;

    /**
     * Total sum of all order items' base prices multiplied by quantity.
     * Represents the initial price before discounts or.ConcurrentModificationException taxes.
     */
    @DecimalMin("0.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal totalBasePrice;

    /**
     * Overall discount rate applied to the order, as a decimal (e.g., 0.20 for 20%).
     * Calculated as totalDiscountAmount divided by totalBasePrice.
     */
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @Column(precision = 5, scale = 2)
    private BigDecimal totalDiscount;

    /**
     * Total sum of all order items' discount amounts.
     * Represents the total monetary value of discounts applied.
     */
    @DecimalMin("0.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal totalDiscountAmount;

    /**
     * Total sum of all order items' discounted prices.
     * Represents the price after discounts but before taxes.
     */
    @DecimalMin("0.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal totalDiscountedPrice;

    /**
     * Effective tax rate for the entire order, as a decimal (e.g., 0.18 for 18%).
     * Calculated as totalTaxAmount divided by totalDiscountedPrice.
     */
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @Column(precision = 5, scale = 2)
    private BigDecimal totalTaxRate;

    // Sum of all cart item taxAmount
    @DecimalMin("0.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal totalTaxAmount;

    /**
     * Shipping cost for the order.
     * Charged by the shipping carrier, defaults to zero if not specified.
     */
    @DecimalMin("0.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal shippingCost; // shipping_charges

    /**
     * Final amount payable by the customer.
     * Calculated as totalDiscountedPrice + totalTaxAmount + shippingCost.
     */
    @DecimalMin("0.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal totalOrderPrice;

    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;

    private String shippingCarrier;

    private String shippingMethod;

    private LocalDateTime deliveredAt;

    // Return related fields
    // One-to-One return mapping
    @JsonManagedReference
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private ReturnOrder returnOrder;

    /**
     * Exchange related fields
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExchangeOrder> exchangeOrders = new ArrayList<>();

    // Payment & Shiprocket fields
    private String razorpayOrderId; // ID from Razorpay order creation
    private String razorpayPaymentId; // ID received after payment completion
    private String paymentId; // Our internal payment tracking ID
    private String paymentStatus; // Payment status tracking
    private LocalDateTime paymentDate; // When payment was completed

    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;

    @Column(name = "is_cod_order")
    private boolean isCodOrder;

    @Column(name = "shiprocket_order_id")
    private Long shiprocketOrderId;

    @Column(name = "shiprocket_shipment_id")
    private Long shiprocketShipmentId;

    @Column(name = "awb_number")
    private String shiprocketAwbNumber; // awb_code

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "courier_name")
    private String shiprocketCourierName;

    @Column(name = "shiprocket_courier_company_id")
    private Integer shiprocketCourierCompanyId;

    private String shippingLabelUrl;
    private String manifestUrl;
    private String invoiceUrl;

    /**
     * In this field setting pickup scheduled date
     * we store the date when the pickup was scheduled.
     */
    @Column(name = "pickup_scheduled_date")
    private String pickupScheduled; 

    @Column(name = "shiprocket_status")
    private String shiprocketStatus;

    @Column(name = "shiprocket_status_code")
    private Integer shiprocketStatusCode;

    @Column(name = "shiprocket_onboarding_completed_now")
    private Integer shiprocketOnboardingCompletedNow;

    @Column(name = "last_tracking_update")
    private LocalDateTime lastTrackingUpdate;

    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;

    @Column(name = "delivery_attempts")
    private Integer deliveryAttempts = 0;

    @Column(name = "last_mile_status")
    private String lastMileStatus;

    @Column(name = "delivery_exception")
    private String deliveryException;

    @Column(name = "ndr_status")
    private String ndrStatus;

    @Column(name = "ndr_comments")
    private String ndrComments;

    // package dimensions

    @Column(name = "package_length")
    private Double packageLength;

    @Column(name = "package_breadth")
    private Double packageBreadth;

    @Column(name = "package_height")
    private Double packageHeight;

    @Column(name = "package_weight")
    private Double packageWeight;

    @Column(name = "preferred_delivery_slot")
    private String preferredDeliverySlot;

    @Column(name = "delivery_instructions")
    private String deliveryInstructions;

    @Column(name = "shipment_status")
    private String shipmentStatus;

    private LocalDateTime lastNDRUpdate;
    private String channelId;
    private String channelOrderId;

    private String errorCode;

    private String errorMessage;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void setBillingAddressAndCreateSnapshot(UserAddress address) {
        this.billingAddress = address;
        this.billingAddressSnapshot = new BillingAddressSnapshot(address);
        this.billingAddressSnapshot.setOrder(this);
    }

    // Helper method to generate order number
    @PrePersist
    protected void onCreate() {
        if (orderNumber == null) {
            // Generate a unique order number with prefix ORD- and current timestamp +
            // Random String
            // Example: ORD-1234567890ABCD
            orderNumber = "ORD-" + System.currentTimeMillis() +
                    UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        }
    }

    // Add helper method
    public boolean isCodOrder() {
        return PaymentMode.COD.equals(paymentMode) || isCodOrder;
    }
}
