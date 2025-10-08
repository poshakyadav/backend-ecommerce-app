package com.ecommerce.bkd.repositories;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.bkd.dtos.products.ProductVariantWithPriceDTO;
import com.ecommerce.bkd.models.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> , JpaSpecificationExecutor<ProductVariant> {

    List<ProductVariant> findByProductId(Long productId);

    boolean existsBySku(String sku);

    @Query("SELECT pv FROM ProductVariant pv JOIN pv.color c WHERE c.name = :color AND pv.active = true")
    List<ProductVariant> findByColorName(@Param("color") String color);

    @Query("SELECT pv FROM ProductVariant pv JOIN pv.sizePrices sp WHERE sp.size = :size AND pv.active = true")
    List<ProductVariant> findBySize(@Param("size") String size);

    List<ProductVariant> findByNameContainingIgnoreCase(String name);
}