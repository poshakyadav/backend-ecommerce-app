package com.ecommerce.bkd.services;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.bkd.models.ProductCategory;

public interface ProductCategoryService {

    ProductCategory createCategory(ProductCategory category, MultipartFile image);
    ProductCategory updateCategory(Long id, ProductCategory updatedCategory, MultipartFile image);
    void deleteCategory(Long id);
    ProductCategory getCategory(Long id);
    List<ProductCategory> getAllCategories();

}
