package com.ecommerce.bkd.services.impl;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.ecommerce.bkd.dtos.shiprocket.ReturnOrderItem;
import com.ecommerce.bkd.dtos.shiprocket.ReturnOrderRequest;
import com.ecommerce.bkd.models.BillingAddressSnapshot;
import com.ecommerce.bkd.models.Order;
import com.ecommerce.bkd.models.PickupLocation;
import com.ecommerce.bkd.models.ProductVariant;
import com.ecommerce.bkd.models.ReturnOrder;
import com.ecommerce.bkd.records.returnOrder.ReturnOrderDTO;
import com.ecommerce.bkd.repositories.PickupLocationRepository;
import com.ecommerce.bkd.services.ReturnOrderMapper;
import com.ecommerce.bkd.services.ShiprocketChannelService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReturnOrderMapperImpl implements ReturnOrderMapper {

    private final ShiprocketChannelService channelService;
    private final PickupLocationRepository pickupRepo;

   
    @Override
    public ReturnOrderDTO toReturnOrderDTO(ReturnOrder ro) {
        ReturnOrderDTO.ReturnPaymentDetailsDTO paymentDetailsDTO = null;
        if (ro.getPaymentDetails() != null) {
            paymentDetailsDTO = new ReturnOrderDTO.ReturnPaymentDetailsDTO(
                    ro.getPaymentDetails().getId(),
                    ro.getPaymentDetails().getPaymentMethodType(),
                    ro.getPaymentDetails().getBankAccountNumber(),
                    ro.getPaymentDetails().getBankIfscCode(),
                    ro.getPaymentDetails().getBankAccountHolderName(),
                    ro.getPaymentDetails().getUpiId()
            );
        }
        return new ReturnOrderDTO(
                ro.getId(),
                ro.getOrder().getId(),
                ro.getReturnStatus(),
                ro.getPaymentStatus(),
                ro.getReason(),
                ro.getDescription(),
                ro.getRejectReason(),
                ro.getRefundAmount(),
                ro.getRefundedAt(),
                ro.getShiprocketReturnOrderId(),
                ro.getShiprocketReturnShipmentId(),
                ro.getShiprocketReturnAwbCode(),
                paymentDetailsDTO,
                ro.getRequestedAt(),
                ro.getApprovedAt(),
                ro.getRejectedAt(),
                ro.getReturnedAt(),
                ro.getCreatedAt(),
                ro.getUpdatedAt()
        );
    }

    @Override
    public ReturnOrderRequest toShiprocketReturnOrderRequest(ReturnOrder ro) {
        Order order = ro.getOrder();
        BillingAddressSnapshot bill = order.getBillingAddressSnapshot();
        PickupLocation warehouse = pickupRepo.findByIsPrimaryLocationTrue()
                .orElseThrow(() -> new IllegalStateException("No primary pickup location configured"));

        try {
            ReturnOrderRequest req = new ReturnOrderRequest();
            // Mandatory top-level fields
            req.setOrderId("R-" + order.getOrderNumber()); // Prefix to ensure uniqueness
            req.setOrderDate(ro.getRequestedAt() != null ? ro.getRequestedAt().toLocalDate().toString() : LocalDate.now().toString());
            req.setChannelId(String.valueOf(channelService.getDefaultChannelId()));

            // Pickup details (customer)
            req.setPickupCustomerName(order.getUser().getFirstName());
            req.setPickupLastName(order.getUser().getLastName() != null ? order.getUser().getLastName() : "");
            req.setPickupAddress(bill.getAddress1());
            req.setPickupAddress2(bill.getAddress2() != null ? bill.getAddress2() : "");
            req.setPickupCity(bill.getCity());
            req.setPickupState(bill.getState());
            req.setPickupCountry(bill.getCountry());
            req.setPickupPincode(Integer.parseInt(bill.getPinCode()));
            req.setPickupEmail(order.getUser().getEmail());
            req.setPickupPhone(order.getUser().getPhone());
            req.setPickupIsdCode("91");
            req.setCompanyName(warehouse.getCompanyName());

            // Shipping details (warehouse)
            req.setShippingCustomerName(warehouse.getPickupLocation());
            req.setShippingLastName("");
            req.setShippingAddress(warehouse.getAddress());
            req.setShippingAddress2(warehouse.getAddress2() != null ? warehouse.getAddress2() : "");
            req.setShippingCity(warehouse.getCity());
            req.setShippingState(warehouse.getState());
            req.setShippingCountry(warehouse.getCountry());
            req.setShippingPincode(Integer.parseInt(warehouse.getPincode()));
            req.setShippingEmail(warehouse.getEmail());
            req.setShippingIsdCode("91");
            req.setShippingPhone(Long.parseLong(warehouse.getPhone()));

            // Items
            req.setOrderItems(order.getItems().stream().map(i -> {
                ProductVariant variant = i.getVariant();
                String imageUrl = variant.getImageUrls().isEmpty() ? null : variant.getImageUrls().get(0);
                return new ReturnOrderItem(
                    variant.getName(),
                    variant.getSku(),
                    i.getQuantity(),
                    i.getDiscountedPrice().intValue(),
                    i.getDiscountAmount() != null ? i.getDiscountAmount().intValue() : 0,
                    ro.getReason(),
                    true,
                    variant.getName(),
                    imageUrl,
                    variant.getProduct().getBrand(),
                    variant.getColor().getName(), // Use VariantColor
                    i.getSelectedSize(),
                    null,
                    null,
                    null,
                    false,
                    true,
                    false,
                    "yes"
                );
            }).toList());

            // Payment & totals
            req.setPaymentMethod("PREPAID"); // Always PREPAID for return orders
            req.setTotalDiscount(order.getTotalDiscountAmount() != null ? order.getTotalDiscountAmount().toString() : "0");
            BigDecimal subTotal = order.getTotalDiscountedPrice().add(order.getTotalTaxAmount());
            req.setSubTotal(subTotal.longValue());

            // Package dimensions
            // Package dimensions
            float defaultDimension = 10.0f;
            float defaultWeight = 1.0f;
            req.setLength(order.getPackageLength() != null && order.getPackageLength() > 0 ? order.getPackageLength().floatValue() : defaultDimension);
            req.setBreadth(order.getPackageBreadth() != null && order.getPackageBreadth() > 0 ? order.getPackageBreadth().floatValue() : defaultDimension);
            req.setHeight(order.getPackageHeight() != null && order.getPackageHeight() > 0 ? order.getPackageHeight().floatValue() : defaultDimension);
            req.setWeight(order.getPackageWeight() != null && order.getPackageWeight() > 0 ? order.getPackageWeight().floatValue() : defaultWeight);

            return req;
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid pincode or phone number format: " + e.getMessage());
        }
    }
}
