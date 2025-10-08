package com.ecommerce.bkd.controllers.admin;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.bkd.records.exchangeOrder.ExchangeOrderDTO;
import com.ecommerce.bkd.services.impl.AdminExchangeService;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/exchanges")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminExchangeController {

    private final AdminExchangeService service;

    @GetMapping("/pending")
    public List<ExchangeOrderDTO> getPendingExchanges() {
        return service.getPendingExchanges();
    }

    @PostMapping("/{exchangeId}/approve")
    public ExchangeOrderDTO approveExchange(@PathVariable Long exchangeId) {
        return service.approveExchange(exchangeId);
    }

    @PostMapping("/{exchangeId}/reject")
    public ExchangeOrderDTO rejectExchange(
        @PathVariable Long exchangeId,
        @RequestParam @NotBlank String reason
    ) {
        return service.rejectExchange(exchangeId, reason);
    }
}