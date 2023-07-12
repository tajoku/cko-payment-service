package com.checkout.cko.exception;

public class AlreadyExistsException extends RuntimeException {

    public AlreadyExistsException(String error) {
        super(error);
    }
}
