package com.dima.shortener_service.service;


import com.dima.shortener_service.dto.*;
import com.dima.shortener_service.entity.Link;
import com.dima.shortener_service.entity.OutboxEvent;
import com.dima.shortener_service.exception.LinkExpiredException;
import com.dima.shortener_service.exception.LinkNotFoundException;
import com.dima.shortener_service.repository.LinkRepository;
import com.dima.shortener_service.repository.OutboxEventRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
public class LinkService {
    private final LinkRepository linkRepository;
    private final OutboxEventRepository outboxEventRepository;

    private final Counter linksClickCounter;
    private final Counter linksCreateCounter;

    private final ObjectMapper objectMapper;

    @Value("${app.base-url}")
    private String baseUrl;

    public LinkService(LinkRepository linkRepository,
                       OutboxEventRepository outboxEventRepository,
                       MeterRegistry meterRegistry,
                       ObjectMapper objectMapper) {
        this.linkRepository = linkRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;

        this.linksClickCounter = Counter.builder("links.clicks")
                .description("Total number of link clicks")
                .register(meterRegistry);

        this.linksCreateCounter = Counter.builder("shortener.links.created")
                .description("Total number of links created")
                .register(meterRegistry);
    }

    public LinkResponse createLink(CreateLinkRequest request) {
        log.info("Link crated with original URL: {}", request.getOriginalUrl());

        Link link = Link.builder()
                .originalUrl(request.getOriginalUrl())
                .expiresAt(countDownExpiration(request.getDaysToExpire()))
                .shortCode(generateShortCode())
                .build();

        linkRepository.save(link);

        LinkResponse response = new LinkResponse();
        response.setShortUrl(buildShortUrl(link.getShortCode()));
        response.setShortCode(link.getShortCode());
        response.setExpiresAt(link.getExpiresAt());

        linksCreateCounter.increment();

        return response;
    }

    @Cacheable(value = "links", key = "#shortCode")
    public String getOriginalUrl(String shortCode) {
        Link link = linkRepository
                .findByShortCode(shortCode)
                .orElseThrow(() -> new LinkNotFoundException("Link not found"));

        if (link.isExpired()) {
            log.warn("Link expired with shortcode: {}", shortCode);
            throw new LinkExpiredException("Link has expired");
        }

        return link.getOriginalUrl();
    }

    @Transactional
    public void publishLinkClickedEvent(String shortCode,
                                        String originalUrl,
                                        String userAgent) {
        incrementClicks(shortCode);
        saveToOutbox(shortCode, originalUrl, userAgent);
        linksClickCounter.increment();
    }

    private void incrementClicks(String shortCode) {
        Link link = linkRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new LinkNotFoundException("Link not found"));
        link.setClicks(link.getClicks() + 1);
        linkRepository.save(link);
    }

    private void saveToOutbox(String shortCode, String originalUrl, String userAgent) {
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

    public LinkInfoResponse getLinkInfo(String shortCode) {
        Link link = linkRepository
                .findByShortCode(shortCode)
                .orElseThrow(
                        () -> new LinkNotFoundException("Link not found")
                );

        LinkInfoResponse response = new LinkInfoResponse();
        response.setOriginalUrl(link.getOriginalUrl());
        response.setShortCode(link.getShortCode());
        response.setClickCount(link.getClicks());
        response.setCreatedAt(link.getCreatedAt());
        response.setExpiresAt(link.getExpiresAt());
        response.setShortUrl(buildShortUrl(link.getShortCode()));

        return response;
    }

    @Transactional
    @CacheEvict(value = "links", key = "#shortCode")
    public void deleteLink(String shortCode) {
        log.info("Deleting link with short code: {}", shortCode);
        linkRepository.deleteByShortCode(shortCode);
    }

    private String generateShortCode() {
        String shortCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        while (linkRepository.existsByShortCode(shortCode)) {
            shortCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        }
        return shortCode;
    }

    private Instant countDownExpiration(Integer expiresAt) {
        return expiresAt != null ? Instant.now().plus(expiresAt, ChronoUnit.DAYS) : null;
    }

    private String buildShortUrl(String shortCode) {
        return baseUrl + "/" + shortCode;
    }
}
