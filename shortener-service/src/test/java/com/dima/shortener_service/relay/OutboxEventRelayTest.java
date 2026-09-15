package com.dima.shortener_service.relay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import org.springframework.data.domain.PageRequest;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import tools.jackson.databind.ObjectMapper;

import java.util.List;

import com.dima.shortener_service.entity.OutboxEvent;
import com.dima.shortener_service.dto.LinkClickedEvent;
import com.dima.shortener_service.producer.LinkEventProducer;
import com.dima.shortener_service.repository.OutboxEventRepository;

@ExtendWith(MockitoExtension.class)
class OutboxEventRelayTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private LinkEventProducer linkEventProducer;

    @Mock
    private ObjectMapper objectMapper;

    private OutboxEventRelay outboxRelay;

    @BeforeEach
    void setUp() {
        outboxRelay = new OutboxEventRelay(
                outboxEventRepository,
                linkEventProducer,
                objectMapper,
                new SimpleMeterRegistry()
        );
    }

    @Test
    void processOutbox_whenKafkaAvailable_marksSent() throws Exception {
        OutboxEvent event = buildPendingEvent();
        LinkClickedEvent clickedEvent = buildClickedEvent();

        when(outboxEventRepository.findByStatus(
                eq(OutboxEvent.OutboxStatus.PENDING), any(PageRequest.class)))
                .thenReturn(List.of(event));
        when(objectMapper.readValue(event.getPayload(), LinkClickedEvent.class))
                .thenReturn(clickedEvent);

        outboxRelay.processOutboxEvents();

        verify(linkEventProducer).produceLinkEvent(clickedEvent);
        assertThat(event.getStatus()).isEqualTo(OutboxEvent.OutboxStatus.SENT);
        assertThat(event.getSentAt()).isNotNull();
        verify(outboxEventRepository).save(event);
    }

    @Test
    void processOutbox_whenKafkaFails_keepsPending() throws Exception {
        OutboxEvent event = buildPendingEvent();
        LinkClickedEvent clickedEvent = buildClickedEvent();

        when(outboxEventRepository.findByStatus(
                eq(OutboxEvent.OutboxStatus.PENDING), any(PageRequest.class)))
                .thenReturn(List.of(event));
        when(objectMapper.readValue(event.getPayload(), LinkClickedEvent.class))
                .thenReturn(clickedEvent);
        doThrow(new RuntimeException("Kafka broker unavailable"))
                .when(linkEventProducer).produceLinkEvent(any());

        assertThatNoException().isThrownBy(() -> outboxRelay.processOutboxEvents());

        assertThat(event.getStatus()).isEqualTo(OutboxEvent.OutboxStatus.PENDING);
        assertThat(event.getSentAt()).isNull();
        verify(outboxEventRepository, never()).save(event);
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
                "abc123", "https://example.com",
                "2024-01-01T00:00:00Z", "Mozilla/5.0", "corr-1");
    }
}
