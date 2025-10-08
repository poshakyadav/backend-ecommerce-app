package com.ecommerce.bkd.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Data;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @OneToOne(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProductReview review;

    @Column(name = "exchange_order_id")
    private Long exchangeOrderId; // Link to ExchangeOrder

    // New field to track review eligibility
    @Column(name = "is_review_eligible")
    private boolean isReviewEligible = true;

    // Quantity of the product ordered
    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull
    @Column(nullable = false)
    private String selectedSize; // e.g., "M"

    @NotNull
    @DecimalMin("0.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal basePrice; // Snapshot of SizePrice.basePrice

    // Discount applied to the product (e.g., 0.10 for 10% discount)
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private BigDecimal discount;

    // quantity * (basePrice * discount)
    // Discount amount of the product
    private BigDecimal discountAmount;

    // quantity * (basePrice - discountAmount)
    // Price after applying discount
    // Calculated as basePrice * (1 - discount)
    @DecimalMin("0.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal discountedPrice;

    // Tax rate applied to the product (e.g., 0.18 for 18% GST)
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private BigDecimal taxRate;

    // Tax amount for the product
    // Calculated as quantity * (discountedPrice * taxRate)
    @DecimalMin("0.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal taxAmount;

    // Total price for the product including tax
    // Calculated as quantity * (discountedPrice + taxAmount)
    @DecimalMin("0.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal totalPrice;

    // Audit fields
   @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    private void calculatePricing() {
        if (discount == null) discount = BigDecimal.ZERO;
        if (taxRate == null) taxRate = BigDecimal.ZERO;

        BigDecimal qty = BigDecimal.valueOf(quantity);
        discountAmount = basePrice.multiply(discount).multiply(qty);
        discountedPrice = basePrice.subtract(basePrice.multiply(discount)).multiply(qty);
        taxAmount = discountedPrice.multiply(taxRate);
        totalPrice = discountedPrice.add(taxAmount);
    }
}
