package com.ecommerce.bkd.services;

import java.util.List;
import com.ecommerce.bkd.models.PickupLocation;
import com.ecommerce.bkd.dtos.shipping.PickupLocationRequest;

public interface PickupLocationService {
    List<PickupLocation> getAllPickupLocations();

    PickupLocation addPickupLocation(PickupLocationRequest request);

    void validateServiceability(String pincode);
}
