package com.ecommerce.bkd.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.bkd.dtos.shiprocket.ShiprocketExchangeOrderResponse;
import com.ecommerce.bkd.exceptions.ResourceNotFoundException;
import com.ecommerce.bkd.models.DeletedReviewHistory;
import com.ecommerce.bkd.models.ExchangeOrder;
import com.ecommerce.bkd.models.ExchangeOrderStatusHistory;
import com.ecommerce.bkd.models.Order;
import com.ecommerce.bkd.models.OrderItem;
import com.ecommerce.bkd.models.ProductReview;
import com.ecommerce.bkd.models.ProductVariant;
import com.ecommerce.bkd.models.SizePrice;
import com.ecommerce.bkd.models.enums.ExchangeOrderStatus;
import com.ecommerce.bkd.records.exchangeOrder.ExchangeOrderDTO;
import com.ecommerce.bkd.repositories.DeletedReviewHistoryRepository;
import com.ecommerce.bkd.repositories.ExchangeOrderRepository;
import com.ecommerce.bkd.repositories.OrderItemRepository;
import com.ecommerce.bkd.repositories.OrderRepository;
import com.ecommerce.bkd.repositories.ProductReviewRepository;
import com.ecommerce.bkd.repositories.ProductVariantRepository;
import com.ecommerce.bkd.services.ExchangeOrderMapper;
import com.ecommerce.bkd.services.PaymentService;
import com.ecommerce.bkd.services.ShiprocketExchangeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminExchangeService {

    private final ExchangeOrderRepository exchangeOrderRepository;
    private final OrderRepository orderRepo;
    private final ShiprocketExchangeService shiprocketService;
    private final ExchangeOrderMapper mapper;
    private final ProductVariantRepository variantRepo;
    private final PaymentService paymentService;

    private final ProductReviewRepository reviewRepository;
    private final DeletedReviewHistoryRepository deletedReviewHistoryRepo;
    private final OrderItemRepository orderItemRepo;


    @Transactional
    public ExchangeOrderDTO approveExchange(Long exchangeOrderId) {
        ExchangeOrder eo = exchangeOrderRepository.findById(exchangeOrderId)
            .orElseThrow(() -> new ResourceNotFoundException("Exchange order not found"));

        if (eo.getStatus() != ExchangeOrderStatus.REQUESTED) {
            throw new IllegalStateException("Exchange not in REQUESTED status");
        }

        ProductVariant replacement = eo.getReplacementVariant();
        SizePrice replacementSizePrice = replacement.getSizePrices().stream()
            .filter(sp -> sp.getSize().equals(eo.getReplacementSize()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Replacement size not found: " + eo.getReplacementSize()));
        if (replacementSizePrice.getStock() <= eo.getOrderItem().getQuantity()) {
            eo.setStatus(ExchangeOrderStatus.REJECTED);
            eo.setRejectReason("Replacement variant out of stock");
            exchangeOrderRepository.save(eo);
            throw new IllegalStateException("Replacement variant ID: " + replacement.getId() + " out of stock");
        }

        Order order = eo.getOrder();
        eo.setStatus(ExchangeOrderStatus.APPROVED);
        eo.setApprovedAt(LocalDateTime.now());
        exchangeOrderRepository.save(eo);

        order.getExchangeOrderStatusHistory().add(new ExchangeOrderStatusHistory(
            order,
            ExchangeOrderStatus.APPROVED,
            "Exchange approved by admin",
            LocalDateTime.now()
        ));
        orderRepo.save(order);

        try {
            ShiprocketExchangeOrderResponse sr = shiprocketService.createExchangeOrder(
                mapper.toShiprocketExchangeOrderRequest(eo));

            eo.setShiprocketExchangeOrderId(String.valueOf(sr.getData().getForwardOrders().getOrderId()));
            eo.setShiprocketExchangeShipmentId(String.valueOf(sr.getData().getForwardOrders().getShipmentId()));
            eo.setStatus(ExchangeOrderStatus.INITIATED);
            exchangeOrderRepository.save(eo);

            order.getExchangeOrderStatusHistory().add(new ExchangeOrderStatusHistory(
                order,
                ExchangeOrderStatus.INITIATED,
                "Exchange initiated with Shiprocket order ID: " + sr.getData().getForwardOrders().getOrderId(),
                LocalDateTime.now()
            ));
            orderRepo.save(order);

            shiprocketService.generateExchangeAwb(
                String.valueOf(sr.getData().getForwardOrders().getShipmentId()),
                null,
                true
            );
            eo.setStatus(ExchangeOrderStatus.AWB_PENDING);
            exchangeOrderRepository.save(eo);

            order.getExchangeOrderStatusHistory().add(new ExchangeOrderStatusHistory(
                order,
                ExchangeOrderStatus.AWB_PENDING,
                "Requested AWB for exchange shipment ID: " + sr.getData().getForwardOrders().getShipmentId(),
                LocalDateTime.now()
            ));
            orderRepo.save(order);
        } catch (Exception e) {
            eo.setStatus(ExchangeOrderStatus.FAILED);
            eo.setRejectReason("Shiprocket error: " + e.getMessage());
            exchangeOrderRepository.save(eo);
            throw new RuntimeException("Shiprocket exchange creation failed: " + e.getMessage(), e);
        }

        return mapper.toExchangeOrderDTO(eo);
    }

    @Transactional
    public ExchangeOrderDTO rejectExchange(Long exchangeOrderId, String reason) {
        ExchangeOrder eo = exchangeOrderRepository.findById(exchangeOrderId)
            .orElseThrow(() -> new ResourceNotFoundException("Exchange order not found"));
        Order order = eo.getOrder();
        OrderItem orderItem = eo.getOrderItem();

        eo.setStatus(ExchangeOrderStatus.REJECTED);
        eo.setRejectReason(reason);
        eo.setRejectedAt(LocalDateTime.now());
        exchangeOrderRepository.save(eo);

        // Restore review if it was deleted
        restoreReview(eo, orderItem);

        order.getExchangeOrderStatusHistory().add(new ExchangeOrderStatusHistory(
            order,
            ExchangeOrderStatus.REJECTED,
            "Exchange rejected: " + reason,
            LocalDateTime.now()
        ));
        orderRepo.save(order);

        return mapper.toExchangeOrderDTO(eo);
    }

    @Transactional
    public ExchangeOrderDTO cancelExchange(Long exchangeOrderId, Long userId) {
        ExchangeOrder eo = exchangeOrderRepository.findById(exchangeOrderId)
            .orElseThrow(() -> new ResourceNotFoundException("Exchange order not found"));
        if (!eo.getOrder().getUser().getId().equals(userId)) {
            throw new IllegalStateException("Unauthorized to cancel exchange");
        }
        if (!List.of(ExchangeOrderStatus.PENDING_PAYMENT, ExchangeOrderStatus.REQUESTED).contains(eo.getStatus())) {
            throw new IllegalStateException("Exchange cannot be canceled in current status: " + eo.getStatus());
        }

        Order order = eo.getOrder();
        OrderItem orderItem = eo.getOrderItem();

        eo.setStatus(ExchangeOrderStatus.CANCELLED);
        eo.setCancelReason("Cancelled by user");
        eo.setCancelAt(LocalDateTime.now());
        exchangeOrderRepository.save(eo);

        // Restore review if it was deleted
        restoreReview(eo, orderItem);

        order.getExchangeOrderStatusHistory().add(new ExchangeOrderStatusHistory(
            order,
            ExchangeOrderStatus.CANCELLED,
            "Exchange cancelled by user",
            LocalDateTime.now()
        ));
        orderRepo.save(order);

        return mapper.toExchangeOrderDTO(eo);
    }

    @Transactional
    public void processExchangeCompletion(ExchangeOrder eo) {
        if (eo.getStatus() != ExchangeOrderStatus.IN_TRANSIT) {
            throw new IllegalStateException("Exchange not in IN_TRANSIT status");
        }

        eo.setStatus(ExchangeOrderStatus.COMPLETED);
        eo.setCompletedAt(LocalDateTime.now());
        exchangeOrderRepository.save(eo);

        Order order = eo.getOrder();
        OrderItem orderItem = eo.getOrderItem();

        // Re-enable review eligibility
        orderItem.setReviewEligible(true);
        // orderItem.setExchangeOrderId(null); // Allow new exchanges if needed
        orderItemRepo.save(orderItem);


        order.getExchangeOrderStatusHistory().add(new ExchangeOrderStatusHistory(
            order,
            ExchangeOrderStatus.COMPLETED,
            "Exchange completed",
            LocalDateTime.now()
        ));
        orderRepo.save(order);

        if (eo.getPriceDifference() != null && eo.getPriceDifference().compareTo(BigDecimal.ZERO) < 0) {
            paymentService.initiateExchangeRefund(eo);
        }

        SizePrice replacementSizePrice = eo.getReplacementVariant().getSizePrices().stream()
            .filter(sp -> sp.getSize().equals(eo.getReplacementSize()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Replacement size not found: " + eo.getReplacementSize()));
        int newStock = replacementSizePrice.getStock() - eo.getOrderItem().getQuantity();
        if (newStock < 0) {
            throw new IllegalStateException("Stock cannot be negative for size " + eo.getReplacementSize());
        }
        replacementSizePrice.setStock(newStock);
        variantRepo.save(eo.getReplacementVariant());
    }

    @Transactional(readOnly = true)
    public List<ExchangeOrderDTO> getPendingExchanges() {
        return exchangeOrderRepository.findByStatus(ExchangeOrderStatus.REQUESTED).stream()
            .map(mapper::toExchangeOrderDTO)
            .collect(Collectors.toList());
    }

    private void restoreReview(ExchangeOrder eo, OrderItem orderItem) {
        DeletedReviewHistory history = deletedReviewHistoryRepo.findByExchangeOrderId(eo.getId())
            .orElse(null);
        if (history != null) {
            ProductReview review = reviewRepository.findById(history.getReviewId())
                .orElseThrow(() -> new IllegalStateException("Review not found for restoration"));
            review.setActive(true);
            review.setDeletedAt(null);
            reviewRepository.save(review);

            // Re-enable review eligibility
            orderItem.setReviewEligible(true);
            orderItem.setExchangeOrderId(null);
            orderItemRepo.save(orderItem);

            // Delete history record
            deletedReviewHistoryRepo.delete(history);
        } else {
            orderItem.setReviewEligible(true);
            orderItem.setExchangeOrderId(null);
            orderItemRepo.save(orderItem);
        }
    }
}