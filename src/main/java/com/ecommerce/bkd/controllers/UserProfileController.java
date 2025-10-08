package com.ecommerce.bkd.controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.bkd.dtos.ApiResponse;
import com.ecommerce.bkd.dtos.UserUpdateRequest;
import com.ecommerce.bkd.dtos.PasswordChangeRequest;
import com.ecommerce.bkd.models.User;
import com.ecommerce.bkd.services.UserProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getCurrentUserProfile(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new ApiResponse<User>(
            true,
            "Profile retrieved successfully",
            userProfileService.getUserProfile(user.getId())));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(new ApiResponse<User>(
            true,
            "Profile updated successfully",
            userProfileService.updateProfile(user.getId(), request)));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PasswordChangeRequest request) {
        userProfileService.changePassword(user.getId(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(new ApiResponse<Void>(true, "Password changed successfully"));
    }

    // @GetMapping("/addresses")
    // public ResponseEntity<ApiResponse<List<AddressDTO>>> getUserAddresses(
    //         @AuthenticationPrincipal User user) {
    //     List<AddressDTO> addresses = userProfileService.getUserAddresses(user.getId());
    //     return ResponseEntity.ok(new ApiResponse<>(
    //         true,
    //         "Addresses retrieved successfully",
    //         addresses));
    // }

    // @PostMapping("/addresses")
    // public ResponseEntity<ApiResponse<UserAddress>> addAddress(
    //         @AuthenticationPrincipal User user,
    //         @Valid @RequestBody UserAddress address) {
    //     try {
    //         UserAddress savedAddress = userProfileService.addAddress(user.getId(), address);
            
    //         String message = savedAddress.isServiceable() ?
    //             "Address added successfully" :
    //             "Address added but " + savedAddress.getServiceabilityMessage();
            
    //         return ResponseEntity.ok(new ApiResponse<>(true, message, savedAddress));
    //     } catch (IllegalArgumentException e) {
    //         return ResponseEntity.badRequest()
    //             .body(new ApiResponse<>(false, e.getMessage(), null));
    //     }
    // }

    // @PutMapping("/addresses/{addressId}")
    // public ResponseEntity<ApiResponse<UserAddress>> updateAddress(
    //         @AuthenticationPrincipal User user,
    //         @PathVariable Long addressId,
    //         @Valid @RequestBody UserAddress address) {
    //     return ResponseEntity.ok(new ApiResponse<UserAddress>(
    //         true,
    //         "Address updated successfully",
    //         userProfileService.updateAddress(user.getId(), addressId, address)));
    // }

    // @DeleteMapping("/addresses/{addressId}")
    // public ResponseEntity<ApiResponse<Void>> deleteAddress(
    //         @AuthenticationPrincipal User user,
    //         @PathVariable Long addressId) {
    //     userProfileService.deleteAddress(user.getId(), addressId);
    //     return ResponseEntity.ok(new ApiResponse<Void>(true, "Address deleted successfully"));
    // }
}
