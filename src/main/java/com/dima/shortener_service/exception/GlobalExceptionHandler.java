package com.dima.shortener_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(LinkExpiredException.class)
    public ResponseEntity<ErrorResponse> handleLinkExpiredException(LinkExpiredException ex) {
        return ResponseEntity
                .status(HttpStatus.GONE)
                .body(ErrorResponse.create(ex, HttpStatusCode.valueOf(HttpStatus.GONE.value()), ex.getMessage()));
    }

    @ExceptionHandler(LinkNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLinkNotFoundException(LinkNotFoundException ex){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.create(ex, HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()), ex.getMessage()));
    }
}
