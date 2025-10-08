package com.ecommerce.bkd.controllers;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.bkd.models.User;
import com.ecommerce.bkd.records.exchangeOrder.ExchangeEligibilityResponseDTO;
import com.ecommerce.bkd.records.exchangeOrder.ExchangeOrderDTO;
import com.ecommerce.bkd.records.exchangeOrder.ExchangeRequestDTO;
import com.ecommerce.bkd.services.impl.UserExchangeService;
import com.razorpay.RazorpayException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserExchangeController {

    private final UserExchangeService service;

    @GetMapping("/orders/{orderId}/exchange-eligible")
    public ExchangeEligibilityResponseDTO checkEligibility(
        @AuthenticationPrincipal User user,
        @PathVariable Long orderId
    ) {
        return service.checkExchangeEligibility(user.getId(), orderId);
    }

    @PostMapping("/orders/{orderId}/exchange")
    public List<ExchangeOrderDTO> requestExchange(
        @AuthenticationPrincipal User user,
        @PathVariable Long orderId,
        @Valid @RequestBody ExchangeRequestDTO request
    ) throws RazorpayException {
        return service.requestExchange(user.getId(), orderId, request);
    }

    @GetMapping("/exchanges")
    public List<ExchangeOrderDTO> getExchanges(@AuthenticationPrincipal User user) {
        return service.getUserExchanges(user.getId());
    }
}