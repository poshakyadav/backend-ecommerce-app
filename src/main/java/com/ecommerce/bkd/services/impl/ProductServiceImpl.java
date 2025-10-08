package com.ecommerce.bkd.services.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.bkd.dtos.products.ProductSearchCriteria;
import com.ecommerce.bkd.dtos.products.ProductVariantSummaryDTO;
import com.ecommerce.bkd.dtos.products.ProductVariantWithPriceDTO;
import com.ecommerce.bkd.dtos.products.VariantColorDTO;
import com.ecommerce.bkd.models.Product;
import com.ecommerce.bkd.models.ProductReview;
import com.ecommerce.bkd.models.ProductVariant;
import com.ecommerce.bkd.models.SizePrice;
import com.ecommerce.bkd.productSpecification.ProductSpecification;
import com.ecommerce.bkd.records.products.ProductDetailDTO;
import com.ecommerce.bkd.records.products.ProductReviewDTO;
import com.ecommerce.bkd.records.products.ProductSearchDTO;
import com.ecommerce.bkd.records.products.ProductVariantDTO;
import com.ecommerce.bkd.repositories.ProductRepository;
import com.ecommerce.bkd.repositories.ProductReviewRepository;
import com.ecommerce.bkd.repositories.ProductVariantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductReviewRepository reviewRepository;
    private final ProductReviewService reviewService;

    @Transactional
    public ProductDetailDTO getProductDetails(Long productId, Long variantId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        List<ProductVariant> variants = variantRepository.findByProductId(productId);
        if (variants.isEmpty()) {
            throw new IllegalStateException("No variants available for product");
        }

        ProductVariant defaultVariant = variantId != null
                ? variants.stream()
                    .filter(v -> v.getId().equals(variantId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Variant not found"))
                : variants.stream()
                    .filter(v -> v.getId().equals(product.getDefaultVariantId()))
                    .findFirst()
                    .orElse(variants.get(0));

        if (defaultVariant.getSizePrices().isEmpty()) {
            throw new IllegalStateException("No size prices available for the default variant");
        }

        System.out.println("&&&&&&&&&&&&Default variant: " + defaultVariant.getId() + ", name: " + defaultVariant.getName());
        incrementViewCount(defaultVariant.getId());

        List<ProductVariantDTO> variantDTOs = variants.stream()
                .map(this::mapToVariantDTO)
                .toList();

        Map<String, List<String>> availableAttributes = getAvailableAttributes(productId);

        Page<ProductReviewDTO> reviews = reviewService.getProductReviews(
            productId, null, PageRequest.of(0, 10));

        ProductVariantDTO defaultVariantDTO = mapToVariantDTO(defaultVariant);

        return new ProductDetailDTO(
                product.getId(),
                product.getBrand(),
                new ProductDetailDTO.ProductCategoryDTO(product.getCategory().getId(), product.getCategory().getName()),
                defaultVariantDTO,
                variantDTOs,
                availableAttributes,
                product.getAverageRating(),
                product.getTotalRatings(),
                product.getRatingDistribution(),
                reviews.getContent()
        );
    }

    @Transactional(readOnly = true)
    public Page<ProductVariantSummaryDTO> searchVariants(ProductSearchCriteria criteria) {
        log.info("Searching variants with criteria: {}", criteria);

        ProductSpecification spec = new ProductSpecification(criteria);
        Pageable pageable = createPageable(criteria);

        // Apply specification and custom sorting
        Page<ProductVariant> variantPage = variantRepository.findAll(spec, pageable);

        return variantPage.map(variant -> {
            Product product = variant.getProduct();
            SizePrice defaultSizePrice = variant.getSizePrices().stream()
                .filter(SizePrice::isDefault)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No default size price found for variant: " + variant.getId()));

            return new ProductVariantSummaryDTO(
                variant.getId(),
                product.getId(),
                variant.getName(),
                variant.getImageUrls().isEmpty() ? null : variant.getImageUrls().get(0),
                defaultSizePrice.getTotalPrice(),
                product.getBrand(),
                product.getCategory().getName()
            );
        });
    }

    @Transactional
    private void incrementViewCount(Long variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
            .orElseThrow(() -> new IllegalArgumentException("Variant not found"));
        variant.setViewCount(variant.getViewCount() + 1);

        System.out.println("@@@@@@@@@@@@@@@@Incrementing view count for variant: " + variantId + ", new count: " + variant.getViewCount());
        variantRepository.save(variant);
    }

    private Pageable createPageable(ProductSearchCriteria criteria) {
        Sort sort;
        switch (criteria.getSortBy()) {
            case "price-asc":
                sort = Sort.by(Sort.Order.asc("sizePrices.totalPrice"));
                break;
            case "price-desc":
                sort = Sort.by(Sort.Order.desc("sizePrices.totalPrice"));
                break;
            case "newest":
                sort = Sort.by(Sort.Order.desc("createdAt"));
                break;
            case "relevance":
                sort = Sort.by(Sort.Order.desc("product.averageRating"));
                break;
            default:
                sort = Sort.by(Sort.Order.desc("product.averageRating"));
                break;
        }
        return PageRequest.of(criteria.getPage(), criteria.getSize(), sort);
    }

    @Transactional
    public void updateProductRatingStats(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        List<ProductReview> reviews = reviewRepository.findAllByProductIdAndStatusAndIsActive(productId, ProductReview.ReviewStatus.APPROVED, true);
        long totalRatings = reviews.size();
        BigDecimal averageRating = totalRatings > 0
            ? BigDecimal.valueOf(reviews.stream().mapToInt(ProductReview::getRating).average().orElse(0.0))
                .setScale(1, BigDecimal.ROUND_HALF_UP)
            : BigDecimal.ZERO;

        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            ratingDistribution.put(i, reviews.stream().filter(r -> r.getRating() == rating).count());
        }

        product.setAverageRating(averageRating);
        product.setTotalRatings(totalRatings);
        product.setRatingDistribution(ratingDistribution);
        productRepository.save(product);
    }

    private Map<String, List<String>> getAvailableAttributes(Long productId) {
        Map<String, List<String>> attributes = new HashMap<>();
        List<ProductVariant> variants = variantRepository.findByProductId(productId);
        for (ProductVariant variant : variants) {
            attributes.computeIfAbsent("Color", k -> new ArrayList<>())
                    .add(variant.getColor().getName());
            for (SizePrice sizePrice : variant.getSizePrices()) {
                attributes.computeIfAbsent("Size", k -> new ArrayList<>())
                        .add(sizePrice.getSize());
            }
        }
        attributes.replaceAll((k, v) -> v.stream().distinct().toList());
        return attributes;
    }

    private ProductVariantDTO mapToVariantDTO(ProductVariant variant) {
        VariantColorDTO colorDTO = new VariantColorDTO(
                variant.getColor().getId(),
                variant.getColor().getName(),
                variant.getColor().getHexCode()
        );
        List<ProductVariantDTO.SizePriceDTO> sizePriceDTOs = variant.getSizePrices().stream()
                .map(sp -> new ProductVariantDTO.SizePriceDTO(
                        sp.getId(),
                        sp.getSize(),
                        sp.getBasePrice(),
                        sp.getDiscount(),
                        sp.getDiscountAmount(),
                        sp.getDiscountedPrice(),
                        sp.getTaxRate(),
                        sp.getTaxAmount(),
                        sp.getTotalPrice(),
                        sp.getFinalPrice(),
                        sp.getStock(),
                        sp.isDefault()
                )).toList();
        return new ProductVariantDTO( 
                variant.getId(),
                variant.getSku(),
                variant.getName(),
                variant.getDescription(),
                variant.getImageUrls(),
                colorDTO,
                sizePriceDTOs,
                variant.getLength(),
                variant.getBreadth(),
                variant.getHeight(),
                variant.getWeight(),
                
                variant.isNewArrival(),
                variant.isTrendy(),
                variant.isRecommended(),
                variant.getViewCount(),
                variant.getSalesCount()
        );
    }
}