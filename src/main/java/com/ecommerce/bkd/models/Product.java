package com.ecommerce.bkd.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ProductCategory category; // Product category

    @Column(name = "default_variant_id")
    private Long defaultVariantId; // ID of the default variant

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants = new ArrayList<>(); // All variants

    @NotNull
    @Column(nullable = false)
    private boolean active = true;


    @Column(name = "average_rating", precision = 3, scale = 1)
    private BigDecimal averageRating = BigDecimal.ZERO; // e.g., 4.3

    @Column(name = "total_ratings")
    private Long totalRatings = 0L;

    @ElementCollection
    @CollectionTable(name = "product_rating_distribution", joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "rating")
    @Column(name = "count")
    private Map<Integer, Long> ratingDistribution = new HashMap<>(); // e.g., {5: 100, 4: 50, ...}


    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (!variants.isEmpty() && defaultVariantId == null) {
            defaultVariantId = variants.get(0).getId(); // Set first variant as default
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (!variants.isEmpty() && defaultVariantId == null) {
            defaultVariantId = variants.get(0).getId();
        }
    }
}