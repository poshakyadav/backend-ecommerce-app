package com.ecommerce.bkd.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.bkd.models.ExchangePaymentDetails;

public interface ExchangePaymentDetailsRepository extends JpaRepository<ExchangePaymentDetails, Long> {
    // Additional query methods can be defined here if needed

}
