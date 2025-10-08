package com.ecommerce.bkd.controllers.admin;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.bkd.records.returnOrder.ReturnOrderDTO;
import com.ecommerce.bkd.services.impl.AdminReturnService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/returns")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReturnController {

    private final AdminReturnService adminReturnService;

    @GetMapping("/pending")
    public List<ReturnOrderDTO> listPending() {
        return adminReturnService.listPendingReturns();
    }

    @PostMapping("/{returnId}/approve")
    public ReturnOrderDTO approve(@PathVariable Long returnId) {
        return adminReturnService.approveReturn(returnId);
    }

    /**
     * Reject a return request.
     * * - URL: http://localhost:8080/api/admin/returns/{returnId}/reject
     * * - Method: POST 
     * * Body:
     * {
     *    "reason": "Product is not eligible for return"
     * }
     * * This endpoint allows an admin to reject a return request with a reason.
     * @param returnId
     * @param req
     * @return
     */
    @PostMapping("/{returnId}/reject")
    public ReturnOrderDTO reject(@PathVariable Long returnId,
            @RequestBody Map<String, String> req) {
        return adminReturnService.rejectReturn(returnId, req.get("reason"));
    }
}