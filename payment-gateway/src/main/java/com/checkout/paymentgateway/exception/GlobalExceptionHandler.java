package com.checkout.paymentgateway.exception;

import com.checkout.paymentgateway.api.ApiError;
import com.checkout.paymentgateway.integration.exception.CkoClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Date;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception e) {
        log.debug(e.getClass() + ": " + e.getMessage(), e);
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(httpStatus).body(ApiError.builder()
                .timestamp(new Date(System.currentTimeMillis()))
                .status(httpStatus)
                .message("Something went wrong, please try again later.")
                .build());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundException(NotFoundException e) {
        log.debug(e.getMessage(), e);
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(httpStatus).body(ApiError.builder()
                .timestamp(new Date(System.currentTimeMillis()))
                .status(httpStatus)
                .message(e.getMessage())
                .build());
    }

    @ExceptionHandler(CkoClientException.class)
    public ResponseEntity<ApiError> handleCkoClientException(CkoClientException e) {
        log.debug(e.getMessage(), e);
        HttpStatus httpStatus = HttpStatus.FAILED_DEPENDENCY;
        return ResponseEntity.status(httpStatus).body(ApiError.builder()
                .timestamp(new Date(System.currentTimeMillis()))
                .status(httpStatus)
                .message(e.getMessage())
                .build());
    }

    private ApiError setError(Exception e, HttpStatus httpStatus) {
        return ApiError.builder()
                .timestamp(new Date(System.currentTimeMillis()))
                .status(httpStatus)
                .message(e.getMessage())
                .build();
    }
}
