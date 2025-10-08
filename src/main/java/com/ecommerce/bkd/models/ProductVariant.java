package com.ecommerce.bkd.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "product_variants")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @Column(nullable = false)
    private String name; // e.g., "Men's Crimson T-Shirt"

    @Column(columnDefinition = "TEXT")
    private String description; // Variant-specific description

    @NotNull
    @Column(nullable = false, unique = true)
    private String sku; // e.g., "TSHIRT-RED"

    private String hsnCode; // Harmonized System Nomenclature code

    @NotNull
    @Column(nullable = false)
    private boolean active = true;

    @ElementCollection
    @CollectionTable(name = "variant_images", joinColumns = @JoinColumn(name = "variant_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>(); // Cloudinary image URLs

    @ElementCollection
    @CollectionTable(name = "variant_image_public_ids", joinColumns = @JoinColumn(name = "variant_id"))
    @Column(name = "public_id")
    private List<String> imagePublicIds = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "variant")
    private List<SizePrice> sizePrices = new ArrayList<>(); // Size and price pairs

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "color_id", nullable = false)
    private VariantColor color; // Single color for this variant

    // Package Dimensions
    @NotNull
    @DecimalMin("0.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal length; // in cm

    @NotNull
    @DecimalMin("0.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal breadth; // in cm

    @NotNull
    @DecimalMin("0.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal height; // in cm

    @NotNull
    @DecimalMin("0.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal weight; // in kg
    

    @Column(name = "is_new_arrival")
    private boolean isNewArrival = false;

    @Column(name = "is_trendy")
    private boolean isTrendy = false;

    @Column(name = "is_recommended")
    private boolean isRecommended = false;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "sales_count")
    private Long salesCount = 0L;

    // ============================

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (viewCount == null) viewCount = 0L;
        if (salesCount == null) salesCount = 0L;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}