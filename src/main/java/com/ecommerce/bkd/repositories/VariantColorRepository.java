package com.ecommerce.bkd.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.bkd.models.VariantColor;

public interface VariantColorRepository extends JpaRepository<VariantColor, Long> {
    Optional<VariantColor> findByNameAndHexCode(String name, String hexCode);
}