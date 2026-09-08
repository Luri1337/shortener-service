package com.dima.analytics_service.controller;

import com.dima.analytics_service.dto.AnalyticsResponse;
import com.dima.analytics_service.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<AnalyticsResponse> getAnalytics(@PathVariable String shortCode) {
        return ResponseEntity.ok(analyticsService.getAnalytics(shortCode));
    }

}
