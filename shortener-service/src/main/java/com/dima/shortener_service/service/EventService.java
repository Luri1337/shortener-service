package com.dima.shortener_service.service;

import com.dima.shortener_service.dto.LinkClickedEvent;
import com.dima.shortener_service.entity.OutboxEvent;
import com.dima.shortener_service.repository.OutboxEventRepository;

import org.slf4j.MDC;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

@Service
public class EventService {
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public EventService(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void processEvent(OutboxEvent event) {
        event.setStatus(OutboxEvent.OutboxStatus.SENT);
        event.setSentAt(Instant.now());
        outboxEventRepository.save(event);
    }

    @Transactional
    public void markAsFailed(OutboxEvent event) {
        event.setStatus(OutboxEvent.OutboxStatus.FAILED);
        outboxEventRepository.save(event);
    }

    @Transactional
    public void saveToOutbox(String shortCode, String originalUrl, String userAgent) {
        LinkClickedEvent clickedEvent = new LinkClickedEvent(
                shortCode,
                originalUrl,
                Instant.now().toString(),
                userAgent,
                MDC.get("correlationId")
        );
        try {
            String payload = objectMapper.writeValueAsString(clickedEvent);
            OutboxEvent event = OutboxEvent.builder()
                    .aggregateId(shortCode)
                    .aggregateType("Link")
                    .eventType("LINK_CLICKED")
                    .payload(payload)
                    .status(OutboxEvent.OutboxStatus.PENDING)
                    .build();
            outboxEventRepository.save(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }

    public List<OutboxEvent> getPendingEvents() {
        return outboxEventRepository
                .findByStatus(
                        OutboxEvent.OutboxStatus.PENDING,
                        PageRequest.of(
                                0,
                                100,
                                Sort.by("createdAt").ascending()
                        )
                );
    }
}
