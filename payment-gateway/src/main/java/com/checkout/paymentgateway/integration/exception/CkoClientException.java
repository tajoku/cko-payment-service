package com.checkout.paymentgateway.integration.exception;

public class CkoClientException extends RuntimeException {
    public CkoClientException(String message) {
        super(message);
    }
}
