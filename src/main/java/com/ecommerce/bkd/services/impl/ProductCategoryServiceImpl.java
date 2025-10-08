package com.ecommerce.bkd.services.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.bkd.exceptions.ResourceNotFoundException;
import com.ecommerce.bkd.models.ProductCategory;
import com.ecommerce.bkd.repositories.ProductCategoryRepository;
import com.ecommerce.bkd.services.ProductCategoryService;

@Service
@Transactional
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;

    public ProductCategoryServiceImpl(ProductCategoryRepository categoryRepository, CloudinaryService cloudinaryService) {
        this.categoryRepository = categoryRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    public ProductCategory createCategory(ProductCategory category, MultipartFile image) {
        if (image != null && !image.isEmpty()) {
            String imageUrl = cloudinaryService.uploadSingleImage(image, "categories");
            category.setImageUrl(imageUrl);
        }
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    @Override
    public ProductCategory updateCategory(Long id, ProductCategory updatedCategory, MultipartFile image) {
        ProductCategory category = getCategory(id);
        
        category.setName(updatedCategory.getName());
        category.setDescription(updatedCategory.getDescription());
        category.setAttributes(updatedCategory.getAttributes());
        category.setActive(updatedCategory.isActive());
        
        if (image != null && !image.isEmpty()) {
            if (category.getImageUrl() != null) {
                cloudinaryService.deleteImage(category.getImageUrl());
            }
            String newImageUrl = cloudinaryService.uploadSingleImage(image, "categories");
            category.setImageUrl(newImageUrl);
        }
        
        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(Long id) {
        ProductCategory category = getCategory(id);
        if (category.getImageUrl() != null) {
            cloudinaryService.deleteImage(category.getImageUrl());
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public ProductCategory getCategory(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Override
    public List<ProductCategory> getAllCategories() {
        return categoryRepository.findAll();
    }
}
