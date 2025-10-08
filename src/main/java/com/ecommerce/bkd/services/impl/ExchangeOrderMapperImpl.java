package com.ecommerce.bkd.services.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ecommerce.bkd.dtos.shiprocket.ShiprocketExchangeOrderRequest;
import com.ecommerce.bkd.models.BillingAddressSnapshot;
import com.ecommerce.bkd.models.ExchangeOrder;
import com.ecommerce.bkd.models.Order;
import com.ecommerce.bkd.models.OrderItem;
import com.ecommerce.bkd.models.PickupLocation;
import com.ecommerce.bkd.models.ProductVariant;
import com.ecommerce.bkd.models.SizePrice;
import com.ecommerce.bkd.records.exchangeOrder.ExchangeOrderDTO;
import com.ecommerce.bkd.repositories.PickupLocationRepository;
import com.ecommerce.bkd.services.ExchangeOrderMapper;
import com.ecommerce.bkd.services.ShiprocketChannelService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExchangeOrderMapperImpl implements ExchangeOrderMapper {

    private final ShiprocketChannelService channelService;
    private final PickupLocationRepository pickupRepo;

    @Override
    public ExchangeOrderDTO toExchangeOrderDTO(ExchangeOrder eo) {
        return new ExchangeOrderDTO(
            eo.getId(),
            eo.getOrder().getId(),
            eo.getOrderItem().getId(),
            eo.getReplacementVariant().getId(),
            eo.getStatus(),
            eo.getReason(),
            eo.getDescription(),
            eo.getRejectReason(),
            eo.getPriceDifference(),
            eo.getPaymentId(),
            eo.getPayoutId(),
            eo.getShiprocketExchangeOrderId(),
            eo.getShiprocketExchangeShipmentId(),
            eo.getShiprocketExchangeAwbCode(),
            eo.getRequestedAt(),
            eo.getApprovedAt(),
            eo.getRejectedAt(),
            eo.getCompletedAt(),
            eo.getCreatedAt(),
            eo.getUpdatedAt(),
            eo.getRazorpayOrderId(), // Use stored Razorpay order ID
            eo.getPriceDifference() != null && eo.getPriceDifference().compareTo(BigDecimal.ZERO) > 0 ? eo.getPriceDifference() : null, // Payment amount if applicable
            eo.getOrder().getCurrency(), // Use order currency
            eo.getReplacementSize()
        );
    }

    @Override
    public ExchangeOrderDTO toExchangeOrderDTO(ExchangeOrder eo, String razorpayOrderId, BigDecimal paymentAmount, String currency) {
        return new ExchangeOrderDTO(
            eo.getId(),
            eo.getOrder().getId(),
            eo.getOrderItem().getId(),
            eo.getReplacementVariant().getId(),
            eo.getStatus(),
            eo.getReason(),
            eo.getDescription(),
            eo.getRejectReason(),
            eo.getPriceDifference(),
            eo.getPaymentId(),
            eo.getPayoutId(),
            eo.getShiprocketExchangeOrderId(),
            eo.getShiprocketExchangeShipmentId(),
            eo.getShiprocketExchangeAwbCode(),
            eo.getRequestedAt(),
            eo.getApprovedAt(),
            eo.getRejectedAt(),
            eo.getCompletedAt(),
            eo.getCreatedAt(),
            eo.getUpdatedAt(),
            razorpayOrderId, // Include Razorpay order ID
            paymentAmount, // Include payment amount
            currency, // Include currency
            eo.getReplacementSize()
        );
    }

    @Override
    public ShiprocketExchangeOrderRequest toShiprocketExchangeOrderRequest(ExchangeOrder eo) {
        Order order = eo.getOrder();
        OrderItem item = eo.getOrderItem();
        ProductVariant originalVariant = item.getVariant();
        ProductVariant exchangeVariant = eo.getReplacementVariant();
        BillingAddressSnapshot bill = order.getBillingAddressSnapshot();
        PickupLocation warehouse = pickupRepo.findByIsPrimaryLocationTrue()
            .orElseThrow(() -> new IllegalStateException("No primary pickup location configured"));

        ShiprocketExchangeOrderRequest req = new ShiprocketExchangeOrderRequest();
        try {
            req.setExchangeOrderId("EX-" + order.getOrderNumber());
            req.setReturnOrderId("R-" + order.getOrderNumber());
            req.setOrderDate(LocalDate.now().toString());
            req.setChannelId(String.valueOf(channelService.getDefaultChannelId()));
            req.setPaymentMethod("prepaid");
            req.setReturnReason(eo.getReason());
            SizePrice exchangeSizePrice = exchangeVariant.getSizePrices().stream()
                .filter(sp -> sp.getSize().equals(eo.getReplacementSize()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Replacement size not found: " + eo.getReplacementSize()));

            req.setSubTotal(exchangeSizePrice.getDiscountedPrice().multiply(BigDecimal.valueOf(item.getQuantity())).toString());
            req.setTotalDiscount(item.getDiscountAmount() != null ? item.getDiscountAmount().toString() : "0");

            req.setQcCheck("true");
            req.setSellerPickupLocationId(String.valueOf(warehouse.getSrId()));
            req.setSellerShippingLocationId(String.valueOf(warehouse.getSrId()));

            req.setBuyerPickupFirstName(order.getUser().getFirstName());
            req.setBuyerPickupLastName(order.getUser().getLastName() != null ? order.getUser().getLastName() : "");
            req.setBuyerPickupAddress(bill.getAddress1());
            req.setBuyerPickupAddress2(bill.getAddress2() != null ? bill.getAddress2() : "");
            req.setBuyerPickupCity(bill.getCity());
            req.setBuyerPickupState(bill.getState());
            req.setBuyerPickupCountry(bill.getCountry());
            req.setBuyerPickupPincode(bill.getPinCode());
            req.setBuyerPickupEmail(order.getUser().getEmail());
            req.setBuyerPickupPhone(order.getUser().getPhone());

            req.setBuyerShippingFirstName(warehouse.getPickupLocation());
            req.setBuyerShippingLastName("");
            req.setBuyerShippingAddress(warehouse.getAddress());
            req.setBuyerShippingAddress2(warehouse.getAddress2() != null ? warehouse.getAddress2() : "");
            req.setBuyerShippingCity(warehouse.getCity());
            req.setBuyerShippingState(warehouse.getState());
            req.setBuyerShippingCountry(warehouse.getCountry());
            req.setBuyerShippingPincode(warehouse.getPincode());
            req.setBuyerShippingEmail(warehouse.getEmail());
            req.setBuyerShippingPhone(warehouse.getPhone());

            ShiprocketExchangeOrderRequest.OrderItem orderItem = new ShiprocketExchangeOrderRequest.OrderItem();
            orderItem.setName(originalVariant.getName());
            orderItem.setSku(originalVariant.getSku());
            orderItem.setUnits(String.valueOf(item.getQuantity()));
            orderItem.setSellingPrice(item.getDiscountedPrice().divide(BigDecimal.valueOf(item.getQuantity()), 2, BigDecimal.ROUND_HALF_UP).toString());
            orderItem.setDiscount(item.getDiscountAmount() != null ? item.getDiscountAmount().toString() : "");
            orderItem.setBrand(originalVariant.getProduct().getBrand());
            orderItem.setColor(originalVariant.getColor().getName());
            orderItem.setExchangeItemId(String.valueOf(exchangeVariant.getId()));
            orderItem.setExchangeItemName(exchangeVariant.getName());
            orderItem.setExchangeItemSku(exchangeVariant.getSku());
            orderItem.setQcEnable(true);
            orderItem.setQcProductName(originalVariant.getName());
            orderItem.setQcProductImage(originalVariant.getImageUrls().isEmpty() ? "" : originalVariant.getImageUrls().get(0));
            orderItem.setQcBrand(originalVariant.getProduct().getBrand());
            orderItem.setQcColor(originalVariant.getColor().getName());
            orderItem.setQcSize(item.getSelectedSize());
            orderItem.setQcUsedCheck("1");
            orderItem.setQcSealtagCheck("1");
            orderItem.setQcBrandBox("1");
            orderItem.setQcCheckDamagedProduct("yes");
            req.setOrderItems(List.of(orderItem));

            float defaultDimension = 10.0f;
            float defaultWeight = 1.0f;
            BigDecimal maxLength = originalVariant.getLength().max(exchangeVariant.getLength());
            BigDecimal maxBreadth = originalVariant.getBreadth().max(exchangeVariant.getBreadth());
            BigDecimal maxHeight = originalVariant.getHeight().max(exchangeVariant.getHeight());
            BigDecimal maxWeight = originalVariant.getWeight().max(exchangeVariant.getWeight());
            req.setExchangeLength(maxLength != null && maxLength.floatValue() > 0 ? maxLength.toString() : String.valueOf(defaultDimension));
            req.setExchangeBreadth(maxBreadth != null && maxBreadth.floatValue() > 0 ? maxBreadth.toString() : String.valueOf(defaultDimension));
            req.setExchangeHeight(maxHeight != null && maxHeight.floatValue() > 0 ? maxHeight.toString() : String.valueOf(defaultDimension));
            req.setExchangeWeight(maxWeight != null && maxWeight.floatValue() > 0 ? maxWeight.toString() : String.valueOf(defaultWeight));
            req.setReturnLength(maxLength != null && maxLength.floatValue() > 0 ? maxLength.toString() : String.valueOf(defaultDimension));
            req.setReturnBreadth(maxBreadth != null && maxBreadth.floatValue() > 0 ? maxBreadth.toString() : String.valueOf(defaultDimension));
            req.setReturnHeight(maxHeight != null && maxHeight.floatValue() > 0 ? maxHeight.toString() : String.valueOf(defaultDimension));
            req.setReturnWeight(maxWeight != null && maxWeight.floatValue() > 0 ? maxWeight.toString() : String.valueOf(defaultWeight));

            return req;
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid pincode or phone number format: " + e.getMessage());
        }
    }
}