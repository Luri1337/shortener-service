package com.dima.analytics_service.service;

import com.dima.analytics_service.dto.AnalyticsResponse;
import com.dima.analytics_service.dto.LinkClickedEvent;
import com.dima.analytics_service.entity.ClickEvent;
import com.dima.analytics_service.repository.ClickEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
public class AnalyticsService {
    private final ClickEventRepository clickEventRepository;

    public AnalyticsService(ClickEventRepository clickEventRepository) {
        this.clickEventRepository = clickEventRepository;
    }


    public void processClickEvent(LinkClickedEvent clickedEvent) {
        if (clickEventRepository.existsByEventId(clickedEvent.getEventId())) {
            log.info("Click event with correlationId {} already processed. Skipping.", clickedEvent.getCorrelationId());
            return;
        }

        log.info("Processing click event for short code: {}", clickedEvent.getShortCode());

        ClickEvent clickEvent = ClickEvent.builder()
                .shortCode(clickedEvent.getShortCode())
                .originalUrl(clickedEvent.getOriginalUrl())
                .clickedAt(Instant.parse(clickedEvent.getClickedAt()))
                .userAgent(clickedEvent.getUserAgent())
                .eventId(clickedEvent.getEventId())
                .build();

        try {
            clickEventRepository.save(clickEvent);
            log.info("Click event saved for shortCode: {}", clickedEvent.getShortCode());
        } catch (DataIntegrityViolationException e) {
            log.error("Duplicate event ignored for eventId: {}", clickedEvent.getEventId());
        }

    }

    public AnalyticsResponse getAnalytics(String shortCode) {
        log.info("Getting analytics for short code: {}", shortCode);

        long totalClicks = clickEventRepository.countByShortCode(shortCode);
        Instant lastClickedAt = clickEventRepository.findLastClickedAt(shortCode);
        return new AnalyticsResponse(shortCode, totalClicks, lastClickedAt);
    }
}
