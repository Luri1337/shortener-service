package com.dima.analytics_service.consumer;

import com.dima.analytics_service.dto.LinkClickedEvent;
import com.dima.analytics_service.service.AnalyticsService;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClickEventConsumer {

    private final AnalyticsService analyticsService;

    public ClickEventConsumer(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @KafkaListener(topics = "link-clicks",
            groupId = "analytics-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(LinkClickedEvent clickedEvent) {
        try {
            log.info("Received click event for short code: {}", clickedEvent.getShortCode());
            MDC.put("correlationId",  clickedEvent.getCorrelationId());
            analyticsService.processClickEvent(clickedEvent);
        }finally {
            MDC.clear();
        }
    }
}
