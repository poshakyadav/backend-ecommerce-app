package com.ecommerce.bkd.controllers.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ecommerce.bkd.dtos.ApiResponse;
import com.ecommerce.bkd.dtos.shipping.PickupLocationRequest;
import com.ecommerce.bkd.models.PickupLocation;
import com.ecommerce.bkd.services.PickupLocationService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/shipping/pickup-locations")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class SrPickupAddressesController {

    private final PickupLocationService pickupLocationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PickupLocation>>> getAllPickupLocations() {
        List<PickupLocation> locations = pickupLocationService.getAllPickupLocations();
        return ResponseEntity.ok(new ApiResponse<>(true, "Pickup locations fetched successfully", locations));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PickupLocation>> addPickupLocation(
            @Valid @RequestBody PickupLocationRequest request) {
        PickupLocation location = pickupLocationService.addPickupLocation(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Pickup location added successfully", location));
    }

    @GetMapping("/validate/{pincode}")
    public ResponseEntity<ApiResponse<Boolean>> validateServiceability(
            @PathVariable String pincode) {
        try {
            pickupLocationService.validateServiceability(pincode);
            return ResponseEntity.ok(new ApiResponse<>(true, "Pincode is serviceable", true));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, e.getMessage(), false));
        }
    }
}
