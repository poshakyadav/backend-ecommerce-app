package com.ecommerce.bkd.controllers;

import java.util.List;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.bkd.models.User;
import com.ecommerce.bkd.records.CreateOrderRequest;
import com.ecommerce.bkd.records.OrderDTO;
import com.ecommerce.bkd.records.order.CreateDirectOrderRequest;
import com.ecommerce.bkd.services.impl.UserOrderService;
import com.razorpay.RazorpayException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user/orders")
@RequiredArgsConstructor
public class UserOrderController {
    private final UserOrderService userOrderService;

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody CreateOrderRequest req,
            @AuthenticationPrincipal User me) throws BadRequestException, RazorpayException {
        OrderDTO dto = userOrderService.createOrder(me.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/buy-now")
    public ResponseEntity<OrderDTO> createDirectOrder(@RequestBody CreateDirectOrderRequest req,
            @AuthenticationPrincipal User me) throws BadRequestException, RazorpayException {
        OrderDTO dto = userOrderService.createDirectOrder(me.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId,
            @AuthenticationPrincipal User me) {
        userOrderService.cancelOrder(me.getId(), orderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> listOrders(@AuthenticationPrincipal User me) {
        return ResponseEntity.ok(userOrderService.listOrders(me.getId()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long orderId,
            @AuthenticationPrincipal User me) {
        return ResponseEntity.ok(userOrderService.getOrder(me.getId(), orderId));
    }
}
