package com.dima.shortener_service.client;

import com.dima.shortener_service.dto.AnalyticsResponse;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class AnalyticsClient {
    private final RestClient restClient;

    public AnalyticsClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public AnalyticsResponse getLinkAnalytics(String shortCode) {
        try {
            return restClient.get()
                    .uri("/api/analytics/{shortCode}", shortCode)
                    .header("X-Correlation-ID", MDC.get("correlationId"))
                    .retrieve()
                    .body(AnalyticsResponse.class);
        } catch (Exception e) {
            log.warn("Analytics service is unavailable: {}", e.getMessage());
            return new AnalyticsResponse(shortCode, 0L, null);
        }
    }
}
