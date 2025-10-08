package com.ecommerce.bkd.exceptions;

public class OrderCancellationException extends RuntimeException {
    public OrderCancellationException(String message) {
        super(message);
    }

    public OrderCancellationException(String message, Throwable cause) {
        super(message, cause);
    }

}
