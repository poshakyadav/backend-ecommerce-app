package com.ecommerce.bkd.exceptions;

public class NdrIntegrationException extends RuntimeException {

    public NdrIntegrationException(String message) {
        super(message);
    }

    public NdrIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }

}
