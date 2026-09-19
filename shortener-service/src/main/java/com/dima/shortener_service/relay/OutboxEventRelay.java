package com.dima.shortener_service.relay;

import com.dima.shortener_service.dto.LinkClickedEvent;
import com.dima.shortener_service.entity.OutboxEvent;
import com.dima.shortener_service.producer.LinkEventProducer;
import com.dima.shortener_service.repository.OutboxEventRepository;
import com.dima.shortener_service.service.EventService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Slf4j
@Component
public class OutboxEventRelay {
    private final EventService eventService;
    private final LinkEventProducer linkEventProducer;
    private final ObjectMapper objectMapper;

    private final Counter outboxProcessingErrors;

    public OutboxEventRelay(EventService eventService,
                            LinkEventProducer linkEventProducer,
                            ObjectMapper objectMapper,
                            MeterRegistry meterRegistry) {
        this.eventService = eventService;
        this.linkEventProducer = linkEventProducer;
        this.objectMapper = objectMapper;
        this.outboxProcessingErrors = Counter.builder("outbox.processing.errors")
                .description("Number of failed outbox event processing attempts")
                .register(meterRegistry);
    }

    @Scheduled(fixedDelay = 5000)
    public void processOutboxEvents() {
        List<OutboxEvent> events = eventService.getPendingEvents();

        for (OutboxEvent event : events) {
            try {
                LinkClickedEvent clickedEvent = objectMapper.readValue(
                        event.getPayload(), LinkClickedEvent.class);

                linkEventProducer.produceLinkEvent(clickedEvent);

                eventService.processEvent(event);

                log.info("Successfully processed outbox event with ID: {}", event.getId());
            } catch (JsonProcessingException e) {
                outboxProcessingErrors.increment();
                log.error(
                        "Invalid payload in outbox event: {}. Error: {}",
                        event.getId(),
                        e.getMessage(),
                        e
                );
                eventService.markAsFailed(event);
            } catch (Exception e) {
                outboxProcessingErrors.increment();
                log.error(
                        "Failed to process outbox event with ID: {}. Error: {}",
                        event.getId(),
                        e.getMessage(),
                        e
                );
            }
        }
    }
}
