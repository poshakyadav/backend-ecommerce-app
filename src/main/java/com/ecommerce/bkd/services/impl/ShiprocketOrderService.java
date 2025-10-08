package com.ecommerce.bkd.services.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ecommerce.bkd.dtos.shiprocket.ShiprocketOrder;
import com.ecommerce.bkd.dtos.shiprocket.ShiprocketOrderDetailResponse;
import com.ecommerce.bkd.dtos.shiprocket.ShiprocketOrderItem;
import com.ecommerce.bkd.dtos.shiprocket.ShiprocketResponse;
import com.ecommerce.bkd.dtos.shiprocket.CreateOrderResponse;
import com.ecommerce.bkd.dtos.shiprocket.OrderListRequest;
import com.ecommerce.bkd.exceptions.ServiceabilityException;
import com.ecommerce.bkd.exceptions.ShiprocketApiException;
import com.ecommerce.bkd.models.Order;
import com.ecommerce.bkd.models.PickupLocation;
import com.ecommerce.bkd.repositories.OrderRepository;
import com.ecommerce.bkd.repositories.PickupLocationRepository;
import com.ecommerce.bkd.services.ShiprocketChannelService;
import com.ecommerce.bkd.utils.QueryUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiprocketOrderService {

    private final ShiprocketApiService apiService;
    private final OrderRepository orderRepository;
    private final PickupLocationRepository pickupLocationRepository;
    private final ShiprocketChannelService channelService;

    // POST Create Order
    @Retryable
    public CreateOrderResponse createOrder(Order order) {
        try {
            // map your domain Order to the Shiprocket DTO
            ShiprocketOrder shipOrder = mapToShiprocketOrder(order);

            // call Shiprocket API
            CreateOrderResponse response = apiService.post(
                    "/orders/create/adhoc", shipOrder, CreateOrderResponse.class);

            // persist Shiprocket's numeric ID back into your Order entity
            order.setShiprocketOrderId(response.getOrderId());
            order.setShiprocketShipmentId(response.getShipmentId());
            order.setShiprocketAwbNumber(response.getAwbCode());
            order.setShiprocketStatus(response.getStatus());
            order.setShiprocketStatusCode(response.getStatusCode());
            order.setShiprocketOnboardingCompletedNow(response.getOnboardingCompletedNow());
            // order.setShiprocketCourierCompanyId(response.getCourierCompanyId());
            // order.setShiprocketCourierName(response.getCourierName());

            orderRepository.save(order);

            return response;
        } catch (Exception e) {
            throw new ShiprocketApiException("Failed to create order: " + e.getMessage(),
                    e);
        }
    }

    public void cancelOrders(List<Long> orderIds) {
        try {
            apiService.post(
                    "/orders/cancel",
                    java.util.Map.of("ids", orderIds),
                    new ParameterizedTypeReference<ShiprocketResponse<Void>>() {
                    });
        } catch (Exception e) {
            throw new ShiprocketApiException("Failed to cancel orders: " + e.getMessage(), e);
        }
    }

    public void cancelOrder(Long orderId) {
        cancelOrders(List.of(orderId));
    }

    public ShiprocketOrder updateOrder(ShiprocketOrder updateRequest) {
        try {
            return apiService.post(
                    "/orders/update/adhoc",
                    updateRequest,
                    ShiprocketOrder.class);
        } catch (Exception e) {
            log.error("Error updating Shiprocket order: ", e);
            throw new ShiprocketApiException("Failed to update order: " + e.getMessage());
        }
    }

    // Get all Orders
    public List<ShiprocketOrder> listOrders(OrderListRequest request) {
        Map<String, Object> params = new HashMap<>();

        if (request.getPage() != null) {
            params.put("page", request.getPage());
        }
        if (request.getPerPage() != null) {
            params.put("per_page", request.getPerPage());
        }
        if (request.getFilter() != null) {
            params.put("filter", request.getFilter());
        }
        if (request.getFilterBy() != null) {
            params.put("filter_by", request.getFilterBy());
        }
        if (request.getSort() != null) {
            params.put("sort", request.getSort());
        }
        if (request.getSortBy() != null) {
            params.put("sort_by", request.getSortBy());
        }
        if (request.getFromDate() != null) {
            params.put("from", request.getFromDate().toString());
        }
        if (request.getToDate() != null) {
            params.put("to", request.getToDate().toString());
        }
        if (request.getAwbNumber() != null) {
            params.put("awb", request.getAwbNumber());
        }

        return apiService.get("/orders?" + QueryUtils.buildQueryParams(params),
                new ParameterizedTypeReference<List<ShiprocketOrder>>() {
                });
    }

    public ShiprocketOrderDetailResponse getOrderDetails(Long shiprocketOrderId) {
        if (shiprocketOrderId == null || shiprocketOrderId <= 0) {
            throw new IllegalArgumentException("shiprocketOrderId must be a positive number");
        }
        try {
            String endpoint = "/orders/show/" + shiprocketOrderId;
            return apiService.get(endpoint, ShiprocketOrderDetailResponse.class);
        } catch (Exception e) {
            log.error("Failed to fetch Shiprocket order details for ID {}: {}", shiprocketOrderId, e.getMessage());
            throw new ShiprocketApiException("Failed to get order details: " + e.getMessage(), e);
        }
    }

    private ShiprocketOrder mapToShiprocketOrder(Order order) {
        ShiprocketOrder shipOrder = new ShiprocketOrder();

        // Required top-level fields
        // Map order details
        // shipOrder.setOrderId(order.getId().toString());
        shipOrder.setOrderId(order.getOrderNumber());
        shipOrder.setOrderDate(order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        shipOrder.setChannelId(channelService.getDefaultChannelId());
        shipOrder.setComment(order.getOrderNotes());

        // 1. Origin (Pickup Location) from primary configured address
        PickupLocation primary = pickupLocationRepository
                .findByIsPrimaryLocationTrue()
                .orElseThrow(() -> new ServiceabilityException(
                        "Primary pickup location not configured"));

        shipOrder.setPickupLocation(primary.getPickupLocation());

        // Map billing details
        shipOrder.setBillingCustomerName(order.getUser().getFirstName());
        shipOrder.setBillingLastName(order.getUser().getLastName());
        shipOrder.setBillingEmail(order.getUser().getEmail());
        shipOrder.setBillingPhone(order.getContactPhone());
        shipOrder.setBillingAlternatePhone(Optional.ofNullable(order.getAlternativePhone()).orElse(""));

        // Map Billing address
        var billing = order.getBillingAddressSnapshot();
        shipOrder.setBillingAddress(billing.getAddress1());
        shipOrder.setBillingAddress2(Optional.ofNullable(billing.getAddress2()).orElse(""));
        shipOrder.setBillingCity(billing.getCity());
        shipOrder.setBillingState(billing.getState());
        shipOrder.setBillingCountry(billing.getCountry());
        shipOrder.setBillingPincode(Integer.valueOf(billing.getPinCode()));

        // Shipping is billing
        shipOrder.setShippingIsBilling(true);

        // Items
        List<ShiprocketOrderItem> items = order.getItems().stream().map(item -> {
            ShiprocketOrderItem si = new ShiprocketOrderItem();
            si.setName(item.getVariant().getName());
            si.setSku(Optional.ofNullable(item.getVariant().getSku()).orElse(""));
            si.setUnits(item.getQuantity());
            si.setSellingPrice(item.getDiscountedPrice().divide(BigDecimal.valueOf(item.getQuantity()), 2, RoundingMode.HALF_UP));
            si.setDiscount(item.getDiscountAmount().divide(BigDecimal.valueOf(item.getQuantity()), 2, RoundingMode.HALF_UP));
            si.setTax(item.getTaxAmount().divide(BigDecimal.valueOf(item.getQuantity()), 2, RoundingMode.HALF_UP));
            si.setHsn(Optional.ofNullable(item.getVariant().getHsnCode()).orElse(""));
            return si;
        }).collect(Collectors.toList());

        shipOrder.setOrderItems(items);

        /**
         * The method of payment. Can be either COD (Cash on delivery) Or Prepaid.
         */
        shipOrder.setPaymentMethod(order.isCodOrder() ? "COD" : "Prepaid");
        shipOrder.setShippingCharges(Optional.ofNullable(order.getShippingCost()).orElse(BigDecimal.ZERO));
        shipOrder.setGiftwrapCharges(BigDecimal.ZERO);
        shipOrder.setTransactionCharges(BigDecimal.ZERO);
        shipOrder.setTotalDiscount(Optional.ofNullable(order.getTotalDiscountAmount()).orElse(BigDecimal.ZERO));

        /**
         * **required** sub_total
         */
        shipOrder.setSubTotal(
               Optional.ofNullable(order.getTotalOrderPrice()).orElse(BigDecimal.ZERO));

        // package dimensions
        shipOrder.setLength(BigDecimal.valueOf(order.getPackageLength()));
        shipOrder.setBreadth(BigDecimal.valueOf(order.getPackageBreadth()));
        shipOrder.setHeight(BigDecimal.valueOf(order.getPackageHeight()));
        shipOrder.setWeight(BigDecimal.valueOf(order.getPackageWeight()));

        return shipOrder;
    }
}
