package com.dima.analytics_service.controller;

import com.dima.analytics_service.dto.AnalyticsResponse;
import com.dima.analytics_service.service.AnalyticsService;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<AnalyticsResponse> getAnalytics(
            @PathVariable String shortCode,
            @RequestHeader(value = "X-Correlation-ID") String correlationId) {
        try {
            MDC.put("correlationId", correlationId);
            return ResponseEntity.ok(analyticsService.getAnalytics(shortCode));
        } finally {
            MDC.clear();
        }

    }

}
