package com.checkout.cko.exception;

import com.checkout.cko.api.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception e) {
        log.debug(e.getClass() + ": " + e.getMessage(), e);
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(httpStatus).body(ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(httpStatus)
                .message("Something went wrong, please try again later.")
                .build());
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ApiError> handleAlreadyExistsException(AlreadyExistsException e) {
        log.debug(e.getMessage(), e);
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus).body(setError(e, httpStatus));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundException(NotFoundException e) {
        log.debug(e.getMessage(), e);
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(httpStatus).body(setError(e, httpStatus));
    }

    @ExceptionHandler(CardExpiredException.class)
    public ResponseEntity<ApiError> handleCardExpired(CardExpiredException e) {
        log.debug(e.getMessage(), e);
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus).body(setError(e, httpStatus));
    }


    @ExceptionHandler(CardNotActiveException.class)
    public ResponseEntity<ApiError> handleCardNotActive(CardNotActiveException e) {
        log.debug(e.getMessage(), e);
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus).body(setError(e, httpStatus));
    }

    @ExceptionHandler(InvalidCurrencyException.class)
    public ResponseEntity<ApiError> handleInvalidCurrency(InvalidCurrencyException e) {
        log.debug(e.getMessage(), e);
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus).body(setError(e, httpStatus));
    }

    private ApiError setError(Exception e, HttpStatus httpStatus) {
        return ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(httpStatus)
                .message(e.getMessage())
                .build();
    }
}
