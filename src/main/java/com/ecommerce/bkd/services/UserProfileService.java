package com.ecommerce.bkd.services;

import com.ecommerce.bkd.models.User;
import com.ecommerce.bkd.dtos.UserUpdateRequest;


public interface UserProfileService {
    User getUserProfile(Long userId);
    User updateProfile(Long userId, UserUpdateRequest request);
    void changePassword(Long userId, String oldPassword, String newPassword);
    // UserAddress addAddress(Long userId, UserAddress address);
    // UserAddress updateAddress(Long userId, Long addressId, UserAddress address);
    // void deleteAddress(Long userId, Long addressId);
    // PaymentMethodDTO addPaymentMethod(Long userId, PaymentMethodRequest request);
    // List<AddressDTO> getUserAddresses(Long userId);
    // List<PaymentMethodDTO> getUserPaymentMethods(Long userId);
    // void deletePaymentMethod(Long userId, Long paymentMethodId);
    // PaymentMethod setDefaultPaymentMethod(Long userId, Long paymentMethodId);
}
