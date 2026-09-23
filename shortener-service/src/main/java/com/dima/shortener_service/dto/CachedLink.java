package com.dima.shortener_service.dto;

import java.io.Serializable;
import java.time.Instant;

public record CachedLink(String originalUrl, Instant expiresAt) implements Serializable {
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
}
