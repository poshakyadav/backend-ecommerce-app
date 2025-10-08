package com.ecommerce.bkd.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ecommerce.bkd.models.enums.ExchangeOrderStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "exchange_orders")
public class ExchangeOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replacement_variant_id", nullable = false)
    private ProductVariant replacementVariant;

    @NotNull
    @Column(nullable = false)
    private String replacementSize; // e.g., "M"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExchangeOrderStatus status;

    @Column(nullable = false)
    private String reason;

    private String description;

    private String cancelReason;

    private String rejectReason;

    @Column(precision = 10, scale = 2)
    private BigDecimal priceDifference; // Positive: user pays; Negative: refund

    private String paymentId; // Razorpay payment ID (if payment made)

    private String payoutId; // Razorpay payout ID (if refund processed)

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "payment_details_id")
    private ExchangePaymentDetails paymentDetails;

    private String shiprocketExchangeOrderId;
    private String shiprocketExchangeShipmentId;
    private String shiprocketExchangeAwbCode;

    private String razorpayOrderId; // Razorpay order ID

    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime cancelAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime completedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        requestedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}