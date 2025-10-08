package com.ecommerce.bkd.controllers.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.bkd.dtos.ApiResponse;
import com.ecommerce.bkd.dtos.products.AdminProductListDTO;
import com.ecommerce.bkd.dtos.products.ProductBasicUpdateDTO;
import com.ecommerce.bkd.dtos.products.ProductUpdateDTO;
import com.ecommerce.bkd.dtos.products.VariantImageUpdateDTO;
import com.ecommerce.bkd.dtos.products.VariantUpdateDTO;
import com.ecommerce.bkd.records.products.ProductCreateDTO;
import com.ecommerce.bkd.records.products.ProductDetailDTO;
import com.ecommerce.bkd.services.impl.AdminProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final AdminProductService productAdminService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminProductListDTO>>> getAllProducts() {
        List<AdminProductListDTO> products = productAdminService.getAllProducts();
        ApiResponse<List<AdminProductListDTO>> response = new ApiResponse<>(
            true,
            products.isEmpty() ? "No products found" : "Products retrieved successfully",
            products
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/variant/{variantId}")
    public ResponseEntity<ApiResponse<ProductDetailDTO>> getProductByVariantId(@PathVariable Long variantId) {
        ProductDetailDTO response = productAdminService.getProductByVariantId(variantId);
        ApiResponse<ProductDetailDTO> apiResponse = new ApiResponse<>(true, "Product details retrieved successfully", response);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{productId}/toggle-active")
    public ResponseEntity<ApiResponse<Void>> toggleProductActive(
            @PathVariable Long productId,
            @RequestParam boolean active) {
        productAdminService.toggleProductActive(productId, active);
        ApiResponse<Void> response = new ApiResponse<>(true, "Product status updated successfully", null);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/variants/{variantId}/toggle-active")
    public ResponseEntity<ApiResponse<Void>> toggleVariantActive(
            @PathVariable Long variantId,
            @RequestParam boolean active) {
        productAdminService.toggleVariantActive(variantId, active);
        ApiResponse<Void> response = new ApiResponse<>(true, "Variant status updated successfully", null);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDetailDTO>> createProduct(
            @Valid @RequestPart("product") ProductCreateDTO dto,
            @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles) {
        Map<Integer, List<MultipartFile>> variantImages = groupImagesByVariant(dto, imageFiles);
        // Validate that each variant has at least one image
        for (int i = 0; i < dto.variants().size(); i++) {
            if (!variantImages.containsKey(i) || variantImages.get(i).isEmpty()) {
                throw new IllegalArgumentException("At least one image is required for variant " + i);
            }
        }
        ProductDetailDTO response = productAdminService.createProduct(dto, variantImages);
        ApiResponse<ProductDetailDTO> apiResponse = new ApiResponse<>(true, "Product created successfully", response);
        return ResponseEntity.status(201).body(apiResponse);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailDTO>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestPart("product") ProductUpdateDTO dto,
            @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles) {
        if (!productId.equals(dto.id())) {
            throw new IllegalArgumentException("Product ID mismatch");
        }
        Map<Integer, List<MultipartFile>> newVariantImages = groupImagesByNewVariant(dto, imageFiles);
        if (!dto.newVariants().isEmpty()) {
            for (int i = 0; i < dto.newVariants().size(); i++) {
                if (!newVariantImages.containsKey(i) || newVariantImages.get(i).isEmpty()) {
                    throw new IllegalArgumentException("At least one image is required for new variant " + i);
                }
            }
        }
        ProductDetailDTO response = productAdminService.updateProduct(productId, dto, newVariantImages);
        ApiResponse<ProductDetailDTO> apiResponse = new ApiResponse<>(true, "Product updated successfully", response);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{productId}/basic")
    public ResponseEntity<ApiResponse<ProductDetailDTO>> updateProductBasic(
            @PathVariable Long productId,
            @Valid @RequestBody ProductBasicUpdateDTO dto) {
        if (!productId.equals(dto.getId())) {
            throw new IllegalArgumentException("Product ID mismatch");
        }
        ProductUpdateDTO updateDTO = new ProductUpdateDTO(dto.getId(), dto.getBrand(), dto.getCategoryId(), List.of());
        ProductDetailDTO response = productAdminService.updateProduct(productId, updateDTO, Map.of());
        ApiResponse<ProductDetailDTO> apiResponse = new ApiResponse<>(true, "Product basic details updated successfully", response);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/variants/{variantId}/details")
    public ResponseEntity<ApiResponse<ProductDetailDTO>> updateVariantDetails(
            @PathVariable Long variantId,
            @Valid @RequestBody VariantUpdateDTO dto) {
        ProductDetailDTO response = productAdminService.updateVariantDetails(variantId, dto);
        ApiResponse<ProductDetailDTO> apiResponse = new ApiResponse<>(true, "Variant details updated successfully", response);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/variants/{variantId}/images")
    public ResponseEntity<ApiResponse<ProductDetailDTO>> updateVariantImages(
            @PathVariable Long variantId,
            @Valid @RequestPart("dto") VariantImageUpdateDTO dto,
            @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles) {
        ProductDetailDTO response = productAdminService.updateVariantImages(variantId, dto, imageFiles);
        ApiResponse<ProductDetailDTO> apiResponse = new ApiResponse<>(true, "Variant images updated successfully", response);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long productId) {
        try {
            productAdminService.deleteProduct(productId);
            ApiResponse<Void> response = new ApiResponse<>(true, "Product deleted successfully", null);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            ApiResponse<Void> response = new ApiResponse<>(false, e.getMessage(), null);
            return ResponseEntity.status(400).body(response);
        }
    }

    @DeleteMapping("/variants/{variantId}")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(@PathVariable Long variantId) {
        try {
            productAdminService.deleteVariant(variantId);
            ApiResponse<Void> response = new ApiResponse<>(true, "Variant deleted successfully", null);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            ApiResponse<Void> response = new ApiResponse<>(false, e.getMessage(), null);
            return ResponseEntity.status(400).body(response);
        }
    }

    private Map<Integer, List<MultipartFile>> groupImagesByVariant(ProductCreateDTO dto, List<MultipartFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return new HashMap<>();
        }
        Map<Integer, List<MultipartFile>> variantImages = new HashMap<>();
        for (int i = 0; i < dto.variants().size(); i++) {
            List<MultipartFile> images = new ArrayList<>();
            for (MultipartFile file : imageFiles) {
                if (file.getOriginalFilename() != null && file.getOriginalFilename().startsWith("variant_" + i + "_")) {
                    images.add(file);
                }
            }
            if (!images.isEmpty()) {
                variantImages.put(i, images);
            }
        }
        return variantImages;
    }

    private Map<Integer, List<MultipartFile>> groupImagesByNewVariant(ProductUpdateDTO dto, List<MultipartFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return new HashMap<>();
        }
        Map<Integer, List<MultipartFile>> variantImages = new HashMap<>();
        for (int i = 0; i < dto.newVariants().size(); i++) {
            List<MultipartFile> images = new ArrayList<>();
            for (MultipartFile file : imageFiles) {
                if (file.getOriginalFilename() != null && file.getOriginalFilename().startsWith("new_variant_" + i + "_")) {
                    images.add(file);
                }
            }
            if (!images.isEmpty()) {
                variantImages.put(i, images);
            }
        }
        return variantImages;
    }
}