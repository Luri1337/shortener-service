package com.dima.shortener_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(LinkExpiredException.class)
    public ResponseEntity<ErrorResponse> handleLinkExpiredException(LinkExpiredException ex) {
        log.warn("Link expired: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.GONE)
                .body(ErrorResponse.create(ex, HttpStatusCode.valueOf(HttpStatus.GONE.value()), ex.getMessage()));
    }

    @ExceptionHandler(LinkNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLinkNotFoundException(LinkNotFoundException ex) {
        log.warn("Link not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.create(ex, HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));


        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.create(ex, HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), "An unexpected error occurred"));
    }


}
