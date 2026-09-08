package com.dima.analytics_service.service;

import com.dima.analytics_service.dto.AnalyticsResponse;
import com.dima.analytics_service.dto.LinkClickedEvent;
import com.dima.analytics_service.entity.ClickEvent;
import com.dima.analytics_service.repository.ClickEventRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AnalyticsService {
    private final ClickEventRepository clickEventRepository;

    public AnalyticsService(ClickEventRepository clickEventRepository) {
        this.clickEventRepository = clickEventRepository;
    }


    public void processClickEvent(LinkClickedEvent clickedEvent) {
        ClickEvent clickEvent = ClickEvent.builder()
                .shortCode(clickedEvent.getShortCode())
                .originalUrl(clickedEvent.getOriginalUrl())
                .clickedAt(Instant.parse(clickedEvent.getClickedAt()))
                .userAgent(clickedEvent.getUserAgent())
                .correlationId(clickedEvent.getCorrelationId())
                .build();

        clickEventRepository.save(clickEvent);
    }

    public AnalyticsResponse getAnalytics(String shortCode) {
        long totalClicks = clickEventRepository.countByShortCode(shortCode);
        Instant lastClickedAt = clickEventRepository.findLastClickedAt(shortCode);
        return new AnalyticsResponse(shortCode, totalClicks, lastClickedAt);
    }
}
