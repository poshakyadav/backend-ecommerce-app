package com.ecommerce.bkd.controllers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.bkd.dtos.payment.PaymentVerificationRequest;
import com.ecommerce.bkd.exceptions.ResourceNotFoundException;
import com.ecommerce.bkd.models.Order;
import com.ecommerce.bkd.records.OrderDTO;
import com.ecommerce.bkd.repositories.OrderRepository;
import com.ecommerce.bkd.services.OrderMapper;
import com.ecommerce.bkd.services.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final PaymentService paymentService;
    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;

    /**
     * Verifies a Razorpay payment using the HMAC signature.
     * If valid, updates order status and returns updated OrderDTO.
     */
    /**
     * Verifies a Razorpay payment by validating the HMAC signature and, if valid,
     * updates the order status. Returns the updated OrderDTO.
     *
     * @param request contains razorpayOrderId, razorpayPaymentId,
     *                razorpaySignature, orderId
     * @return 200 OK with OrderDTO if valid, 400 Bad Request if signature invalid,
     *         404 Not Found if order missing, 500 Internal Server Error otherwise
     */
    @PostMapping("/verify")
    public ResponseEntity<OrderDTO> verifyPayment(@RequestBody PaymentVerificationRequest request) {
        try {
            boolean valid = paymentService.verifyPayment(request);
            if (!valid) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
            OrderDTO dto = orderMapper.toOrderDTO(order);
            return ResponseEntity.ok(dto);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
