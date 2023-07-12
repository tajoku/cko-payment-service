package com.checkout.cko.exception;

public class CardExpiredException extends RuntimeException {

    public CardExpiredException(String message) {
        super(message);
    }
}
