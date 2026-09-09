package com.dima.analytics_service.service;

import com.dima.analytics_service.dto.LinkClickedEvent;
import com.dima.analytics_service.entity.ClickEvent;
import com.dima.analytics_service.repository.ClickEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private ClickEventRepository clickEventRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private LinkClickedEvent buildEvent() {
        LinkClickedEvent event = new LinkClickedEvent();
        event.setShortCode("abc12345");
        event.setOriginalUrl("https://google.com");
        event.setClickedAt(Instant.now().toString());
        event.setUserAgent("Mozilla");
        event.setCorrelationId(UUID.randomUUID().toString());
        return event;
    }

    @Test
    void processClickEvent_shouldSaveClickEvent() {
        LinkClickedEvent event = buildEvent();

        analyticsService.processClickEvent(event);

        verify(clickEventRepository, times(1)).save(any(ClickEvent.class));
    }

    @Test
    void processClickEvent_shouldSaveTwoRecords_whenSameShortCode() {
        LinkClickedEvent event = buildEvent();

        analyticsService.processClickEvent(event);
        analyticsService.processClickEvent(event);

        verify(clickEventRepository, times(2)).save(any(ClickEvent.class));
    }
}
