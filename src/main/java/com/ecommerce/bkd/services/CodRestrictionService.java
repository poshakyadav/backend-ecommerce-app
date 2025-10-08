package com.ecommerce.bkd.services;

public interface CodRestrictionService {
    boolean isCodAllowedForPincode(String pincode);
    boolean isCodAllowedForAmount(double amount);
    boolean isCodAllowedForCategory(Long categoryId);
    boolean isCodAllowedForProduct(Long productId);
}
