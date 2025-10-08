package com.ecommerce.bkd.exceptions;

public class WishlistException extends RuntimeException {
    public WishlistException(String message) {
        super(message);
    }

    public WishlistException(String message, Throwable cause) {
        super(message, cause);
    }

    public WishlistException(Throwable cause) {
        super(cause);
    }
}