package com.ecommerce.bkd.services;

import org.json.JSONObject;

import com.ecommerce.bkd.dtos.shiprocket.ShiprocketOrder;
import com.ecommerce.bkd.models.Cart;
import com.ecommerce.bkd.models.Order;
import com.ecommerce.bkd.models.enums.PaymentMode;
import com.ecommerce.bkd.records.CreateOrderRequest;
import com.ecommerce.bkd.records.OrderDTO;

public interface OrderMapper {
    // internal → JPA
    Order toOrderEntity(Long userId, Cart cart, CreateOrderRequest req);

    // JPA → DTO
    OrderDTO toOrderDTO(Order order);

    OrderDTO toOrderDTO(Order order, JSONObject razorOrder);

    OrderDTO toOrderDTO(Order order, ShiprocketOrder shiprocketOrder);

    // helper lookups for serviceability
    String getPickupPincode(); // from your primary PickupLocation

    String getUserPincode(Long addressId); // from UserAddress repo

    String getShippingPincode(Order o); // from billing/shipping snapshot

    double getCartWeight(Cart cart); // sum of item weights

    boolean getPaymentMode(PaymentMode mode);
}