package com.ecommerce.bkd.controllers;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.bkd.dtos.ApiResponse;
import com.ecommerce.bkd.dtos.products.ProductSearchCriteria;
import com.ecommerce.bkd.dtos.products.ProductVariantSummaryDTO;
import com.ecommerce.bkd.records.products.ProductDetailDTO;
import com.ecommerce.bkd.repositories.ProductRepository;
import com.ecommerce.bkd.services.impl.ProductServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class UserProductController {

    private static final Logger logger = LoggerFactory.getLogger(UserProductController.class);

    private final ProductServiceImpl productService;
    private final ProductRepository productRepository;

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailDTO>> getProductDetails(
            @PathVariable Long productId,
            @RequestParam(required = false) Long variantId) {
        ProductDetailDTO dto = productService.getProductDetails(productId, variantId);
        ApiResponse<ProductDetailDTO> apiResponse = new ApiResponse<>(true, "Product details retrieved successfully", dto);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductVariantSummaryDTO>>> searchProducts(
            @RequestParam Map<String, String> allParams) {
        ProductSearchCriteria criteria = new ProductSearchCriteria(allParams);

        // Validation
        if (criteria.getCategories() != null && criteria.getCategories().stream().anyMatch(String::isEmpty)) {
            logger.warn("Invalid categories provided: empty values detected");
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid category filter. Empty values are not allowed.", null));
        }
        // if (criteria.getBrands() != null && criteria.getBrands().stream().anyMatch(String::isEmpty)) {
        //     logger.warn("Invalid brands provided: empty values detected");
        //     return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid brand filter. Empty values are not allowed.", null));
        // }
        if (criteria.getMinPrice() != null && criteria.getMinPrice().compareTo(BigDecimal.ZERO) < 0) {
            logger.warn("Invalid minPrice: negative value detected");
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid minimum price. Must be non-negative.", null));
        }
        if (criteria.getMaxPrice() != null && criteria.getMaxPrice().compareTo(BigDecimal.ZERO) < 0) {
            logger.warn("Invalid maxPrice: negative value detected");
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid maximum price. Must be non-negative.", null));
        }
        if (criteria.getMinPrice() != null && criteria.getMaxPrice() != null && criteria.getMinPrice().compareTo(criteria.getMaxPrice()) > 0) {
            logger.warn("Invalid price range: minPrice > maxPrice");
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Minimum price cannot be greater than maximum price.", null));
        }

        logger.info("Received search request with criteria: {}", criteria);
        Page<ProductVariantSummaryDTO> results = productService.searchVariants(criteria);
        ApiResponse<Page<ProductVariantSummaryDTO>> apiResponse = new ApiResponse<>(true, "Search results retrieved", results);
        logger.info("Returning {} search results for criteria: {}", results.getTotalElements(), criteria);
        return ResponseEntity.ok(apiResponse);
    }

}