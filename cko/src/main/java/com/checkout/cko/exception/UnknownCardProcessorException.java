package com.checkout.cko.exception;

public class UnknownCardProcessorException extends RuntimeException {

    public UnknownCardProcessorException(String error) {
        super(error);
    }
}
