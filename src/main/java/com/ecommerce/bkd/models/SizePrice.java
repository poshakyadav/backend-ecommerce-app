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
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "size_prices")
public class SizePrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @NotNull
    @Column(nullable = false)
    private String size; // e.g., "M", "L", "XL"

    @NotNull
    @DecimalMin("0.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal basePrice; // Price for this size or Product Price, // (Suppose base price is 500.00)

    /*
     * Store discount as a percentage (0.0 to 1.0)
     * Example: 0.10 for 10% discount
     * Calculate it on service layer and then store it (Means if user enters 10% discount, we store 0.10)
     * Take input from frontend like -> 10
     */
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @Column(precision = 5, scale = 2)
    private BigDecimal discount; // Discount percentage (e.g., 0.10 for 10%)

    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount; // basePrice * discount , // (500.00 * 0.10 = 50.00)

    @Column(precision = 10, scale = 2)
    private BigDecimal discountedPrice; // basePrice - discountAmount, // (500.00 - 50.00 = 450.00)

    
    /**
     * This discountedPrice is the totalPrice.
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal totalPrice; // This totalPrice shows on frontend Product Pages or ProductDetailPage, // (450.00)


    /*
     * Store tax rate as a percentage (0.0 to 1.0)
     * Example: 0.18 for 18% GST
     * Calculate it on service layer and then store it (Means if user enters 18% GST, we store 0.18)
     * Take input from frontend like -> 18
     */
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @Column(precision = 5, scale = 2)
    private BigDecimal taxRate; // e.g., 0.18 for 18% GST

    @Column(precision = 10, scale = 2)
    private BigDecimal taxAmount; // totalPrice * taxRate , // (450.00 * 0.18 = 81.00)

    /*
     * This finalPrice is the totalPrice + taxAmount.
     * It shows on frontend Order Summary Page or Checkout Page.
     * Example: 450.00 + 81.00 = 531.00
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal finalPrice; // totalPrice + taxAmount, 450.00 + 81.00 = 531.00

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer stock; // Stock for this size

    @NotNull
    @Column(nullable = false)
    private boolean isDefault = false; // Marks default size

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
        updatedAt = LocalDateTime.now();
        calculatePricing();
    }

    private void calculatePricing() {
        // Ensure discount and taxRate are not null
        if (discount == null) discount = BigDecimal.ZERO;
        if (taxRate == null) taxRate = BigDecimal.ZERO;

        // Calculate discountAmount
        discountAmount = basePrice.multiply(discount).setScale(2, RoundingMode.HALF_UP);
        if (discount.compareTo(BigDecimal.ZERO) == 0) {
            discountAmount = BigDecimal.ZERO;
        }

        // Calculate discountedPrice
        discountedPrice = basePrice.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);

        // Calculate taxAmount
        taxAmount = discountedPrice.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        if (taxRate.compareTo(BigDecimal.ZERO) == 0) {
            taxAmount = BigDecimal.ZERO;
        }

        // Set totalPrice as discountedPrice (as per comment)
        totalPrice = discountedPrice;

        // Calculate finalPrice
        finalPrice = totalPrice.add(taxAmount).setScale(2, RoundingMode.HALF_UP);
    }
}