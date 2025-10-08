package com.ecommerce.bkd.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ecommerce.bkd.models.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.active = true")
    List<String> findDistinctBrands();


}