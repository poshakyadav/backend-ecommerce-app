package com.ecommerce.bkd.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ServiceabilityException extends RuntimeException {
    public ServiceabilityException(String message) {
        super(message);
    }

    public ServiceabilityException(String message, Throwable cause) {
        super(message, cause);
    }
}
