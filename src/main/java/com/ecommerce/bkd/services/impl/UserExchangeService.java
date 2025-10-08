package com.ecommerce.bkd.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.bkd.dtos.payment.PaymentInitiateRequest;
import com.ecommerce.bkd.exceptions.ResourceNotFoundException;
import com.ecommerce.bkd.models.DeletedReviewHistory;
import com.ecommerce.bkd.models.ExchangeOrder;
import com.ecommerce.bkd.models.ExchangeOrderStatusHistory;
import com.ecommerce.bkd.models.ExchangePaymentDetails;
import com.ecommerce.bkd.models.Order;
import com.ecommerce.bkd.models.OrderItem;
import com.ecommerce.bkd.models.ProductReview;
import com.ecommerce.bkd.models.ProductVariant;
import com.ecommerce.bkd.models.SizePrice;
import com.ecommerce.bkd.models.enums.ExchangeOrderStatus;
import com.ecommerce.bkd.models.enums.OrderFulfillmentStatus;
import com.ecommerce.bkd.records.exchangeOrder.ExchangeEligibilityResponseDTO;
import com.ecommerce.bkd.records.exchangeOrder.ExchangeOrderDTO;
import com.ecommerce.bkd.records.exchangeOrder.ExchangeRequestDTO;
import com.ecommerce.bkd.repositories.DeletedReviewHistoryRepository;
import com.ecommerce.bkd.repositories.ExchangeOrderRepository;
import com.ecommerce.bkd.repositories.OrderItemRepository;
import com.ecommerce.bkd.repositories.OrderRepository;
import com.ecommerce.bkd.repositories.ProductReviewRepository;
import com.ecommerce.bkd.repositories.ProductVariantRepository;
import com.ecommerce.bkd.services.ExchangeOrderMapper;
import com.ecommerce.bkd.services.PaymentService;
import com.razorpay.RazorpayException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserExchangeService {

    private final OrderRepository orderRepo;
    private final ExchangeOrderRepository exchangeRepo;
    private final ProductVariantRepository variantRepo;
    private final OrderItemRepository orderItemRepo;
    private final PaymentService paymentService;
    private final ExchangeOrderMapper mapper;

    private final ProductReviewRepository reviewRepository;
    private final DeletedReviewHistoryRepository deletedReviewHistoryRepo;
    private final NotificationService notificationService; // Assume a service for user notifications

    @Transactional(readOnly = true)
    public ExchangeEligibilityResponseDTO checkExchangeEligibility(Long userId, Long orderId) {
        Order order = orderRepo.findByIdAndUserId(orderId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getDeliveredAt() == null || order.getFulfillmentStatus() != OrderFulfillmentStatus.DELIVERED) {
            return new ExchangeEligibilityResponseDTO(false, "Order not delivered", null, List.of());
        }

        if (order.getDeliveredAt().plusDays(7).isBefore(LocalDateTime.now())) {
            return new ExchangeEligibilityResponseDTO(false, "Exchange window expired", order.getDeliveredAt(), List.of());
        }

        List<ExchangeEligibilityResponseDTO.OrderItemDTO> items = order.getItems().stream()
            .filter(item -> item.getExchangeOrderId() == null)
            .map(item -> new ExchangeEligibilityResponseDTO.OrderItemDTO(
                item.getId(),
                item.getVariant().getName(),
                item.getVariant().getSku(),
                item.getSelectedSize() + ", Color: " + item.getVariant().getColor().getName(),
                item.getVariant().getImageUrls()
            ))
            .collect(Collectors.toList());

        return new ExchangeEligibilityResponseDTO(true, "Order eligible for exchange", order.getDeliveredAt(), items);
    }

    @Transactional
    public List<ExchangeOrderDTO> requestExchange(Long userId, Long orderId, ExchangeRequestDTO req) throws RazorpayException {
        Order order = orderRepo.findByIdAndUserId(orderId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getDeliveredAt() == null || order.getFulfillmentStatus() != OrderFulfillmentStatus.DELIVERED) {
            throw new IllegalStateException("Order must be delivered");
        }

        if (order.getDeliveredAt().plusDays(7).isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Exchange window expired");
        }

        // BigDecimal totalPriceDifference = BigDecimal.ZERO;
        BigDecimal accumulatedPriceDifference = BigDecimal.ZERO;
        List<ExchangeOrder> exchangeOrders = new ArrayList<>();

        for (ExchangeRequestDTO.ItemExchange item : req.exchanges()) {
            OrderItem orderItem = orderItemRepo.findById(item.orderItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));
            if (!orderItem.getOrder().getId().equals(orderId) || orderItem.getExchangeOrderId() != null) {
                throw new IllegalStateException("Invalid or already exchanged item: " + item.orderItemId());
            }

            ProductVariant replacementVariant = variantRepo.findById(item.replacementVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Replacement variant not found"));
            if (!replacementVariant.getProduct().getId().equals(orderItem.getVariant().getProduct().getId())) {
                throw new IllegalStateException("Exchange limited to same product for item: " + item.orderItemId());
            }

            SizePrice replacementSizePrice = replacementVariant.getSizePrices().stream()
                .filter(sp -> sp.getSize().equals(item.replacementSize()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Replacement size not found: " + item.replacementSize()));

            if (replacementSizePrice.getStock() < orderItem.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for replacement size: " + item.replacementSize());
            }

            // Handle existing review
            ProductReview review = orderItem.getReview();
            if (review != null) {
                // Backup review to history
                DeletedReviewHistory history = new DeletedReviewHistory();
                history.setReviewId(review.getId());
                history.setOrderItemId(orderItem.getId());
                history.setUserId(review.getUser().getId());
                history.setProductId(review.getProduct().getId());
                history.setRating(review.getRating());
                history.setReviewText(review.getReviewText());
                history.setImageUrls(new ArrayList<>(review.getImageUrls()));
                history.setStatus(review.getStatus());
                history.setHelpfulVotes(review.getHelpfulVotes());
                history.setUnhelpfulVotes(review.getUnhelpfulVotes());
                history.setDeletedReason("Exchange requested: " + req.reason());
                history.setExchangeOrderId(null); // Will be set after saving ExchangeOrder
                deletedReviewHistoryRepo.save(history);

                // Soft-delete review
                review.setActive(false);
                review.setDeletedAt(LocalDateTime.now());
                reviewRepository.save(review);

                // Notify user
                notificationService.notifyUser(userId, "Your previous review for " + orderItem.getVariant().getName() +
                    " has been removed due to your exchange request. You can review the new item after the exchange is completed.");
            }

            // Disable review eligibility during exchange
            orderItem.setReviewEligible(false);
            orderItemRepo.save(orderItem);

            BigDecimal originalPrice = orderItem.getDiscountedPrice().divide(BigDecimal.valueOf(orderItem.getQuantity()), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal replacementPrice = replacementSizePrice.getDiscountedPrice();
            BigDecimal priceDifference = replacementPrice.subtract(originalPrice).multiply(BigDecimal.valueOf(orderItem.getQuantity()));
            // totalPriceDifference = totalPriceDifference.add(priceDifference);
            accumulatedPriceDifference = accumulatedPriceDifference.add(priceDifference);

            ExchangeOrder eo = new ExchangeOrder();
            eo.setOrder(order);
            eo.setOrderItem(orderItem);
            eo.setReplacementVariant(replacementVariant);
            eo.setReplacementSize(item.replacementSize());
            eo.setStatus(ExchangeOrderStatus.PENDING_PAYMENT); // Initial status
            eo.setReason(req.reason());
            eo.setDescription(req.description());
            eo.setPriceDifference(priceDifference);
            eo.setRequestedAt(LocalDateTime.now());

            // Store payment details if refund is required
            if (priceDifference.compareTo(BigDecimal.ZERO) < 0 && req.paymentDetails() != null) {
                ExchangePaymentDetails paymentDetails = new ExchangePaymentDetails();
                paymentDetails.setPaymentMethodType(
                    ExchangePaymentDetails.PaymentMethodType.valueOf(req.paymentDetails().paymentMethodType()));
                paymentDetails.setBankAccountHolderName(req.paymentDetails().bankAccountHolderName());
                paymentDetails.setBankIfscCode(req.paymentDetails().bankIfscCode());
                paymentDetails.setBankAccountNumber(req.paymentDetails().bankAccountNumber());
                paymentDetails.setUpiId(req.paymentDetails().upiId());
                eo.setPaymentDetails(paymentDetails);
            } else if (priceDifference.compareTo(BigDecimal.ZERO) < 0 && req.paymentDetails() == null) {
                throw new IllegalStateException("Payment details required for refund due to price difference: ₹" + priceDifference.abs());
            }

            exchangeOrders.add(eo);
            exchangeRepo.save(eo);
            orderItem.setExchangeOrderId(eo.getId());

            // Update review history with exchange order ID
            if (review != null) {
                DeletedReviewHistory history = deletedReviewHistoryRepo.findByReviewIdAndExchangeOrderId(review.getId(), null)
                    .orElseThrow(() -> new IllegalStateException("Review history not found"));
                history.setExchangeOrderId(eo.getId());
                deletedReviewHistoryRepo.save(history);
            }
        }

        orderItemRepo.saveAll(order.getItems());
        order.getExchangeOrderStatusHistory().add(new ExchangeOrderStatusHistory(
            order,
            ExchangeOrderStatus.PENDING_PAYMENT,
            "Exchange payment pending for " + req.exchanges().size() + " items",
            LocalDateTime.now()
        ));
        orderRepo.save(order);

        // Create a final variable for use in lambda
        final BigDecimal totalPriceDifference = accumulatedPriceDifference;

        // Handle payment if required
        if (totalPriceDifference.compareTo(BigDecimal.ZERO) > 0) {
            JSONObject paymentResponse = paymentService.initiateExchangePayment(
                new PaymentInitiateRequest(orderId, totalPriceDifference, order.getCurrency())
            );
            String razorpayOrderId = paymentResponse.getString("id");

            // Update all exchange orders with the Razorpay order ID
            for (ExchangeOrder eo : exchangeOrders) {
                eo.setRazorpayOrderId(razorpayOrderId);
                exchangeRepo.save(eo);
            }

            // Return DTO with Razorpay order details
            return exchangeOrders.stream()
                .map(eo -> mapper.toExchangeOrderDTO(eo, razorpayOrderId, totalPriceDifference, order.getCurrency()))
                .collect(Collectors.toList());
        } else {
            // No payment required, mark as REQUESTED
            for (ExchangeOrder eo : exchangeOrders) {
                eo.setStatus(ExchangeOrderStatus.REQUESTED);
                exchangeRepo.save(eo);
            }
            order.getExchangeOrderStatusHistory().add(new ExchangeOrderStatusHistory(
                order,
                ExchangeOrderStatus.REQUESTED,
                "Exchange requested for " + req.exchanges().size() + " items",
                LocalDateTime.now()
            ));
            orderRepo.save(order);

            return exchangeOrders.stream()
                .map(mapper::toExchangeOrderDTO)
                .collect(Collectors.toList());
        }
    }

    @Transactional
    public void processExchangePaymentSuccess(ExchangeOrder eo, String paymentId) {
        if (eo.getStatus() != ExchangeOrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Exchange order not in PENDING_PAYMENT status");
        }

        eo.setPaymentId(paymentId);
        eo.setStatus(ExchangeOrderStatus.REQUESTED);
        exchangeRepo.save(eo);

        Order order = eo.getOrder();
        order.getExchangeOrderStatusHistory().add(new ExchangeOrderStatusHistory(
            order,
            ExchangeOrderStatus.REQUESTED,
            "Exchange payment successful, paymentId=" + paymentId,
            LocalDateTime.now()
        ));
        orderRepo.save(order);
    }

    @Transactional(readOnly = true)
    public List<ExchangeOrderDTO> getUserExchanges(Long userId) {
        return exchangeRepo.findByOrderUserId(userId).stream()
            .map(mapper::toExchangeOrderDTO)
            .collect(Collectors.toList());
    }
}