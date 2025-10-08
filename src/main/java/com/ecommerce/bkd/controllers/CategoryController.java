package com.ecommerce.bkd.controllers;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.bkd.dtos.ApiResponse;
import com.ecommerce.bkd.models.ProductCategory;
import com.ecommerce.bkd.services.ProductCategoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final ProductCategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductCategory>>> getAllCategories() {
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Categories retrieved successfully",
            categoryService.getAllCategories()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ProductCategory>>> getAllActiveCategoriesPublic() {
        List<ProductCategory> categories = categoryService.getAllCategories().stream()
            .filter(ProductCategory::isActive)
            .toList();
        return ResponseEntity.ok(new ApiResponse<>(true, "Categories retrieved successfully", categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductCategory>> getCategory(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Category retrieved successfully",
            categoryService.getCategory(id)));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<ProductCategory>> createCategory(
            @RequestPart("category") ProductCategory category,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        ProductCategory createdCategory = categoryService.createCategory(category, image);
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Category created successfully",
            createdCategory));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<ProductCategory>> updateCategory(
            @PathVariable Long id,
            @RequestPart("category") ProductCategory category,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        ProductCategory updatedCategory = categoryService.updateCategory(id, category, image);
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Category updated successfully",
            updatedCategory));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Category deleted successfully"));
    }
}
