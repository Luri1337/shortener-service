package com.dima.shortener_service.relay;

import com.dima.shortener_service.dto.LinkClickedEvent;
import com.dima.shortener_service.entity.OutboxEvent;
import com.dima.shortener_service.producer.LinkEventProducer;
import com.dima.shortener_service.repository.OutboxEventRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
public class OutboxEventRelay {
    private final OutboxEventRepository outboxEventRepository;
    private final LinkEventProducer linkEventProducer;
    private final ObjectMapper objectMapper;

    private final Counter outboxProcessingErrors;

    public OutboxEventRelay(OutboxEventRepository outboxEventRepository,
                            LinkEventProducer linkEventProducer,
                            ObjectMapper objectMapper,
                            MeterRegistry meterRegistry) {
        this.outboxEventRepository = outboxEventRepository;
        this.linkEventProducer = linkEventProducer;
        this.objectMapper = objectMapper;
        this.outboxProcessingErrors = Counter.builder("outbox.processing.errors")
                .description("Number of failed outbox event processing attempts")
                .register(meterRegistry);
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEvent> events = outboxEventRepository
                .findByStatus(
                        OutboxEvent.OutboxStatus.PENDING,
                        PageRequest.of(0, 100)
                );

        for (OutboxEvent event : events) {
            try {
                LinkClickedEvent clickedEvent = objectMapper.readValue(
                        event.getPayload(), LinkClickedEvent.class);

                linkEventProducer.produceLinkEvent(clickedEvent);

                event.setStatus(OutboxEvent.OutboxStatus.SENT);
                event.setSentAt(Instant.now());
                outboxEventRepository.save(event);

                log.info("Successfully processed outbox event with ID: {}", event.getId());
            } catch (Exception e) {
                outboxProcessingErrors.increment();
                log.error(
                        "Failed to process outbox event with ID: {}. Error: {}",
                        event.getId(),
                        e.getMessage()
                );
            }
        }
    }
}
