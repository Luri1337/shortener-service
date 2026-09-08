package com.dima.analytics_service.dto;

import java.time.Instant;

public record AnalyticsResponse(
        String shortCode,
        Long totalClicks,
        Instant lastClickAt
) {}
