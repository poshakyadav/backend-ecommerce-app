package com.ecommerce.bkd.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.bkd.dtos.ApiResponse;
import com.ecommerce.bkd.dtos.user.UserDto;
import com.ecommerce.bkd.models.User;

@RestController
@RequestMapping("/api/users")
public class UserController {

    /**
     * Returns the currently authenticated user as a DTO
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()") // Explicitly state that this endpoint requires authentication
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(
            @AuthenticationPrincipal User user) {
        UserDto dto = new UserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getRole());
        return ResponseEntity.ok(new ApiResponse<>(true, "Current user fetched", dto));
    }
}