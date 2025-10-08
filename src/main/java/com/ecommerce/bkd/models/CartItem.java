package com.ecommerce.bkd.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer quantity;

    @NotNull
    @Column(nullable = false)
    private String selectedSize; // e.g., "M", "L", "XL"

    @NotNull
    @Column(precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(precision = 5, scale = 2)
    private BigDecimal discount; // Percentage (e.g., 10.00 for 10%)

    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountedPrice;

    @Column(precision = 5, scale = 2)
    private BigDecimal taxRate; // Percentage (e.g., 5.00 for 5%)

    @Column(precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal finalPrice; 

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        calculatePricing();
    }

    @PreUpdate
    protected void onUpdate() {
        // ||||||Calling onUpdate||||||
        updatedAt = LocalDateTime.now();
        calculatePricing();
    }

    public void calculatePricing() {
        if (discount == null) discount = BigDecimal.ZERO;
        if (taxRate == null) taxRate = BigDecimal.ZERO;

        BigDecimal qty = BigDecimal.valueOf(quantity);

        // Calculate discountAmount as a decimal fraction
        discountAmount = basePrice.multiply(discount).multiply(qty).setScale(2, RoundingMode.HALF_UP);

        // Calculate discountedPrice
        discountedPrice = basePrice.multiply(qty).subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);

        // Calculate taxAmount as a decimal fraction
        taxAmount = discountedPrice.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);

       // Set totalPrice to discountedPrice (before tax)
       totalPrice = discountedPrice.setScale(2, RoundingMode.HALF_UP);

       // Set finalPrice to discountedPrice + taxAmount
       finalPrice = discountedPrice.add(taxAmount).setScale(2, RoundingMode.HALF_UP);
   
    }
}