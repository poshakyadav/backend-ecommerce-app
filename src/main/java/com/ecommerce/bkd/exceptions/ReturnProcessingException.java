package com.ecommerce.bkd.exceptions;

public class ReturnProcessingException extends RuntimeException {
    public ReturnProcessingException(String message) {
        super(message);
    }

    public ReturnProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
