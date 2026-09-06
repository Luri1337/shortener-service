package com.dima.shortener_service.exception;

public class LinkExpiredException extends RuntimeException {
    public LinkExpiredException(String message) {
        super(message);
    }
}
