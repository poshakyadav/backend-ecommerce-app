package com.ecommerce.bkd.exceptions;

public class ShiprocketApiException extends RuntimeException {
    public ShiprocketApiException(String message) {
        super(message);
    }

    public ShiprocketApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
