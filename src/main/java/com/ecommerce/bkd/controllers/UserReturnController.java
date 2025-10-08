package com.ecommerce.bkd.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.bkd.records.returnOrder.ReturnOrderDTO;
import com.ecommerce.bkd.records.returnOrder.ReturnRequestDTO;
import com.ecommerce.bkd.services.impl.UserReturnService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user/orders")
@RequiredArgsConstructor
public class UserReturnController {

    private final UserReturnService userReturnService;

    /**
     * Request a return for an order.
     * 
     * - Create Return Order with Refund to Bank Account
     *  - URL: http://localhost:8080/api/user/orders/123/return
     *  - Method: POST
     *  - Body: 
     *       ```json
     *      {
     *          "reason": "Product is defective",
     *         "description": "The product stopped working within a week of purchase.",
     *         "paymentDetails": {
     *             "paymentMethodType": "BANK_TRANSFER",
     *            "bankAccountNumber": "1234567890",
     *            "bankIfscCode": "SBIN0001234",
     *            "bankAccountHolderName": "John Doe",
     *            "upiId": null
     *        }
     *      }
     *      ```
     * 
     *  - Create Return Order with Refund via UPI
     *   - URL: http://localhost:8080/api/user/orders/123/return
     *  - Method: POST
     *  - Body:
     *      ```json
     *      {
     *         "reason": "Product is defective",
     *        "description": "The product stopped working within a week of purchase.",
     *        "paymentDetails": {
     *            "paymentMethodType": "UPI",
     *           "bankAccountNumber": null,
     *           "bankIfscCode": null,
     *           "bankAccountHolderName": null,
     *           "upiId": "john.doe@upi"
     *        }
     *      }
     *     ```
     * 
     * 
     * @param orderId
     * @param userId
     * @param req
     * @return
     */
    @PostMapping("/{orderId}/return")
    public ResponseEntity<ReturnOrderDTO> requestReturn(
            @PathVariable Long orderId,
            @AuthenticationPrincipal(expression = "id") Long userId,
            @Valid @RequestBody ReturnRequestDTO req) {
        req.paymentDetails().validate();
        ReturnOrderDTO dto = userReturnService.requestReturn(userId, orderId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}