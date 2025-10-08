package com.ecommerce.bkd.services.impl;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ecommerce.bkd.models.PickupLocation;
import com.ecommerce.bkd.repositories.PickupLocationRepository;
import com.ecommerce.bkd.services.PickupLocationService;

import jakarta.transaction.Transactional;

import com.ecommerce.bkd.dtos.shipping.PickupLocationRequest;
import com.ecommerce.bkd.dtos.shiprocket.ShiprocketResponse;
import com.ecommerce.bkd.exceptions.ServiceabilityException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PickupLocationServiceImpl implements PickupLocationService {

    private final ShiprocketApiService apiService;
    private final PickupLocationRepository pickupLocationRepository;

    /**
     * Fetches all pickup locations from Shiprocket, clears existing entries
     * locally,
     * then saves the fresh set to the database to avoid stale data.
     */
    @Override
    @Transactional
    public List<PickupLocation> getAllPickupLocations() {
        try {
            // Remove all old pickup locations before syncing
            pickupLocationRepository.deleteAll();

            // Fetch all pickup addresses from Shiprocket
            ShiprocketResponse<Map<String, Object>> response = apiService.get(
                    "/settings/company/pickup",
                    new ParameterizedTypeReference<ShiprocketResponse<Map<String, Object>>>() {
                    });

            if (!response.isSuccess()) {
                throw new ServiceabilityException("Failed to fetch pickup locations");
            }

            Map<String, Object> data = response.getData();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rawList = (List<Map<String, Object>>) data.get("shipping_address");

            List<PickupLocation> locations = new ArrayList<>();
            for (Map<String, Object> addressData : rawList) {
                PickupLocation location = mapToPickupLocation(addressData);
                // Populate metadata
                location.setCompanyName((String) data.get("company_name"));

            }

            return locations;
        } catch (Exception e) {
            log.error("Error fetching pickup locations: ", e);
            throw new ServiceabilityException("Failed to fetch pickup locations", e);
        }
    }

    @Override
    public PickupLocation addPickupLocation(PickupLocationRequest request) {
        try {
            ShiprocketResponse<Map<String, Object>> response = apiService.post(
                    "/settings/company/addpickup",
                    request,
                    new ParameterizedTypeReference<ShiprocketResponse<Map<String, Object>>>() {
                    });

            if (!response.isSuccess()) {
                throw new ServiceabilityException("Failed to add pickup location");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> addressData = (Map<String, Object>) response.getData().get("address");

            // Map the core address fields
            PickupLocation location = mapToPickupLocation(addressData);

            // And set the extras from the outer response
            location.setShiprocketPickupId(
                    Long.valueOf(response.getData().get("pickup_id").toString()));
            location.setCompanyName((String) response.getData().get("company_name"));
            location.setFullName((String) response.getData().get("full_name"));

            if (addressData.containsKey("is_primary_location")) {
                location.setIsPrimaryLocation(response.getData().get("is_primary_location") != null
                        ? Boolean.valueOf(response.getData().get("is_primary_location").toString())
                        : null);
            }

            return pickupLocationRepository.save(location);
        } catch (Exception e) {
            log.error("Error adding pickup location: ", e);
            throw new ServiceabilityException("Failed to add pickup location", e);
        }
    }

    @Override
    public void validateServiceability(String pincode) {
        // Implementation for pincode validation
        if (!isValidPincode(pincode)) {
            throw new ServiceabilityException("Invalid pincode format");
        }
    }

    private PickupLocation mapToPickupLocation(Map<String, Object> data) {
        PickupLocation location = new PickupLocation();

        if (data.containsKey("company_id")) {
            location.setCompanyId(data.get("company_id") != null
                    ? Long.valueOf(data.get("company_id").toString())
                    : null);
        }

        if (data.containsKey("pickup_location")) {
            location.setPickupLocation(
                    data.get("pickup_location") != null ? data.get("pickup_location").toString() : null);
        }

        if (data.containsKey("pickup_code")) {
            location.setPickupCode(data.get("pickup_code") != null ? data.get("pickup_code").toString() : null);
        }

        location.setAddress(data.get("address") != null ? data.get("address").toString() : null);
        location.setAddress2(data.get("address_2") != null ? data.get("address_2").toString() : null);
        location.setAddressType(data.get("address_type") != null ? data.get("address_type").toString() : null);

        location.setCity(data.get("city") != null ? data.get("city").toString() : null);
        location.setState(data.get("state") != null ? data.get("state").toString() : null);
        location.setCountry(data.get("country") != null ? data.get("country").toString() : null);

        location.setGstin(data.get("gstin") != null ? data.get("gstin").toString() : null);
        location.setPincode(data.get("pin_code") != null ? data.get("pin_code").toString() : null);
        location.setPhone(data.get("phone") != null ? data.get("phone").toString() : null);
        location.setEmail(data.get("email") != null ? data.get("email").toString() : null);
        location.setName(data.get("name") != null ? data.get("name").toString() : null);

        location.setAlternatePhone(data.get("alternate_phone") != null
                ? data.get("alternate_phone").toString()
                : null);

        location.setLatitude(data.get("lat") != null
                ? getDoubleValue(data.get("lat"))
                : null);
        location.setLongitude(data.get("long") != null
                ? getDoubleValue(data.get("long"))
                : null);

        location.setStatus(data.get("status") != null
                ? Integer.valueOf(data.get("status").toString())
                : null);

        location.setPhoneVerified(data.get("phone_verified") != null
                ? Boolean.valueOf(data.get("phone_verified").toString())
                : null);

        location.setRtoAddressId(
                data.get("rto_address_id") != null ? Long.valueOf(data.get("rto_address_id").toString()) : null);

        if (data.containsKey("associated_rto_address")) {
            location.setAssociatedRtoAddress(
                    data.get("associated_rto_address") != null ? data.get("associated_rto_address").toString() : null);
        }

        if (data.containsKey("extra_info")) {
            location.setExtraInfo(data.get("extra_info") != null ? data.get("extra_info").toString() : null);
        }

        location.setSrId(data.get("id") != null ? Long.valueOf(data.get("id").toString()) : null);

        // Check "is_primary_location" key is exist or not
        if (data.containsKey("is_primary_location")) {
            location.setIsPrimaryLocation(data.get("is_primary_location") != null
                    ? Boolean.valueOf(data.get("is_primary_location").toString())
                    : null);
        }

        if (data.containsKey("created_at") && data.containsKey("updated_at")) {
            location.setCreatedAt(
                    parseDateTime(data.get("created_at") != null ? data.get("created_at").toString() : null));

            location.setUpdatedAt(
                    parseDateTime(data.get("updated_at") != null ? data.get("updated_at").toString() : null));
        }

        return location;
    }

    private Double getDoubleValue(Object value) {
        return value != null ? Double.valueOf(value.toString()) : null;
    }

    private LocalDateTime parseDateTime(String dateStr) {
        try {
            return LocalDateTime.parse(dateStr.replace(" ", "T"));
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isValidPincode(String pincode) {
        return pincode != null && pincode.matches("^[1-9][0-9]{5}$");
    }
}
