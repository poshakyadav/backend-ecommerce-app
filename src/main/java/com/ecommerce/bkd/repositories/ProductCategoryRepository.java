package com.ecommerce.bkd.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.bkd.models.ProductCategory;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    // Custom query methods can be defined here if needed

}
