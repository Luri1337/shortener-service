package com.dima.analytics_service.consumer;

import com.dima.analytics_service.dto.LinkClickedEvent;
import com.dima.analytics_service.service.AnalyticsService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

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
        analyticsService.processClickEvent(clickedEvent);
    }
}
