package com.ecommerce.bkd.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.bkd.dtos.ApiResponse;
import com.ecommerce.bkd.dtos.userAddress.UserAddressDTO;
import com.ecommerce.bkd.models.User;
import com.ecommerce.bkd.services.UserAddressService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user/addresses")
@RequiredArgsConstructor
public class UserAddressController {
    private final UserAddressService userAddressService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserAddressDTO>>> getUserAddresses(@AuthenticationPrincipal User user) {
        List<UserAddressDTO> dto = userAddressService.getUserAddresses(user.getId());
        ApiResponse<List<UserAddressDTO>> apiResponse = new ApiResponse<>(true, "User Addresses retrieved successfully", dto);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserAddressDTO>> addAddress(
            @RequestBody UserAddressDTO addressDTO,
            @AuthenticationPrincipal User user) {
        addressDTO.setUserId(user.getId());
        UserAddressDTO  dto = userAddressService.addAddress(addressDTO);
        ApiResponse<UserAddressDTO> apiResponse = new ApiResponse<>(true, "Address added successfully", dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserAddressDTO>> updateAddress(
            @PathVariable Long id,
            @RequestBody UserAddressDTO addressDTO,
            @AuthenticationPrincipal User user) {
        addressDTO.setId(id);
        addressDTO.setUserId(user.getId());
        UserAddressDTO dto = userAddressService.updateAddress(addressDTO);
        ApiResponse<UserAddressDTO> apiResponse = new ApiResponse<>(true, "Address updated successfully", dto);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        userAddressService.deleteAddress(id, user.getId());
        ApiResponse<Void> apiResponse = new ApiResponse<>(true, "Address deleted successfully", null);
        return ResponseEntity.ok(apiResponse);
    }
}