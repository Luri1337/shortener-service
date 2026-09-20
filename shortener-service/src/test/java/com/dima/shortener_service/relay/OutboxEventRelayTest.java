package com.dima.shortener_service.relay;

import com.dima.shortener_service.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.assertThatNoException;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.dima.shortener_service.entity.OutboxEvent;
import com.dima.shortener_service.dto.LinkClickedEvent;
import com.dima.shortener_service.producer.LinkEventProducer;

@ExtendWith(MockitoExtension.class)
class OutboxEventRelayTest {

    @Mock
    private LinkEventProducer linkEventProducer;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EventService eventService;

    private OutboxEventRelay outboxRelay;

    @BeforeEach
    void setUp() {
        outboxRelay = new OutboxEventRelay(
                eventService,
                linkEventProducer,
                objectMapper,
                new SimpleMeterRegistry()
        );
    }

    @Test
    void processOutbox_whenKafkaAvailable_marksSent() throws Exception {
        OutboxEvent event = buildPendingEvent();
        LinkClickedEvent clickedEvent = buildClickedEvent();

        when(eventService.getPendingEvents()).thenReturn(List.of(event));
        when(objectMapper.readValue(event.getPayload(), LinkClickedEvent.class))
                .thenReturn(clickedEvent);

        outboxRelay.processOutboxEvents();

        verify(linkEventProducer).produceLinkEvent(clickedEvent);
        verify(eventService).processEvent(event);
    }

    @Test
    void processOutbox_whenKafkaFails_keepsPending() throws Exception {
        OutboxEvent event = buildPendingEvent();
        LinkClickedEvent clickedEvent = buildClickedEvent();

        when(eventService.getPendingEvents()).thenReturn(List.of(event));
        when(objectMapper.readValue(event.getPayload(), LinkClickedEvent.class))
                .thenReturn(clickedEvent);
        doThrow(new RuntimeException("Kafka broker unavailable"))
                .when(linkEventProducer).produceLinkEvent(any());

        assertThatNoException().isThrownBy(() -> outboxRelay.processOutboxEvents());

        verify(eventService, never()).processEvent(event);
        verify(eventService, never()).markAsFailed(event);
    }

    private OutboxEvent buildPendingEvent() {
        return OutboxEvent.builder()
                .aggregateId("abc123")
                .aggregateType("Link")
                .eventType("LINK_CLICKED")
                .payload("{\"shortCode\":\"abc123\",\"originalUrl\":\"https://example.com\"}")
                .status(OutboxEvent.OutboxStatus.PENDING)
                .build();
    }

    private LinkClickedEvent buildClickedEvent() {
        return new LinkClickedEvent(
                "abc12345",
                "https://google.com",
                Instant.now().toString(),
                "Mozilla",
                "correlation-id-123",
                UUID.randomUUID().toString()
        );
    }
}