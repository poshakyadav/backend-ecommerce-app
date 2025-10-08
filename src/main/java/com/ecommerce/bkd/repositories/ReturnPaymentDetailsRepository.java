package com.ecommerce.bkd.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.bkd.models.ReturnPaymentDetails;

public interface ReturnPaymentDetailsRepository extends JpaRepository<ReturnPaymentDetails, Long> {

    // Custom query methods can be added here if needed

}
