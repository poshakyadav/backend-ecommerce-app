package com.ecommerce.bkd.exceptions;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductReviewException.class)
    public ResponseEntity<ApiResponse<?>> handleReviewException(ProductReviewException ex) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler({CartException.class, WishlistException.class})
    public ResponseEntity<ApiResponse<?>> handleCustomExceptions(Exception ex) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
            .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(ServiceabilityException.class)
    public ResponseEntity<ApiResponse<?>> handleServiceabilityException(ServiceabilityException ex) {
        return ResponseEntity.badRequest()
            .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(OrderProcessingException.class)
    public ResponseEntity<ApiResponse<?>> handleOrderProcessingException(OrderProcessingException ex) {
        log.error("Order processing error: ", ex);
        return ResponseEntity.badRequest()
            .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ApiResponse<?>> handlePaymentProcessingException(PaymentProcessingException ex) {
        log.error("Payment processing error: ", ex);
        return ResponseEntity.badRequest()
            .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing));
        return ResponseEntity.badRequest()
            .body(new ApiResponse<>(false, "Validation failed", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiResponse<>(false, "An unexpected error occurred: " + ex.getMessage(), null));
    }

    @Data
    static class ApiResponse<T> {
        private final boolean success;
        private final String message;
        private final T data;

        public ApiResponse(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
    }
}
