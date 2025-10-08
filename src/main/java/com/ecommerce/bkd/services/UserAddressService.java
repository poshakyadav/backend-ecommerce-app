package com.ecommerce.bkd.services;

import java.util.List;

import com.ecommerce.bkd.dtos.userAddress.UserAddressDTO;

public interface UserAddressService {
    List<UserAddressDTO> getUserAddresses(Long userId);
    UserAddressDTO addAddress(UserAddressDTO dto);
    UserAddressDTO updateAddress(UserAddressDTO dto);
    void deleteAddress(Long id, Long userId);
}