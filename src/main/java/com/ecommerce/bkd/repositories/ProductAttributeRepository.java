package com.ecommerce.bkd.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.bkd.models.ProductAttribute;

public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {

    @Query("SELECT pa FROM ProductAttribute pa WHERE pa.attributeType = :type AND pa.attributeValue = :value")
    Optional<ProductAttribute> findByTypeAndValue(
            @Param("type") String attributeType,
            @Param("value") String attributeValue);
}