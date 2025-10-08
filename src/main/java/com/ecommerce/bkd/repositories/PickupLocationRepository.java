package com.ecommerce.bkd.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ecommerce.bkd.models.PickupLocation;

public interface PickupLocationRepository extends JpaRepository<PickupLocation, Long> {
    Optional<PickupLocation> findByIsPrimaryLocationTrue();
}
