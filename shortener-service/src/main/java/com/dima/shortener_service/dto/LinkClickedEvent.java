package com.dima.shortener_service.dto;

import java.time.Instant;

public record LinkClickedEvent(
        String shortCode,
        String originalUrl,
        Instant clickedAt,
        String userAgent,
        String correlationId
) {
}
