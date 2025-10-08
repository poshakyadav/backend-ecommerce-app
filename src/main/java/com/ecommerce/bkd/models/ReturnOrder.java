package com.ecommerce.bkd.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.ecommerce.bkd.models.enums.OrderPaymentStatus;
import com.ecommerce.bkd.models.enums.OrderReturnStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
@Table(name = "return_orders")
public class ReturnOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", unique = true, nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    private OrderReturnStatus returnStatus;

    @Enumerated(EnumType.STRING)
    private OrderPaymentStatus paymentStatus;

    private String reason;
    private String description;
    private String rejectReason;

    /**
     * refundAmount = (order.totalDiscountedPrice + order.totalTaxAmount) -
     * order.shippingCost
     */
    private BigDecimal refundAmount;

    private LocalDateTime refundedAt;

    private Long shiprocketReturnOrderId;
    private Long shiprocketReturnShipmentId;
    private String shiprocketReturnAwbCode;

    @OneToOne(mappedBy = "returnOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private ReturnPaymentDetails paymentDetails;

    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime returnedAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

}
