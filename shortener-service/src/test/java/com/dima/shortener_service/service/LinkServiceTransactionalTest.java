package com.dima.shortener_service.service;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.dima.shortener_service.entity.Link;
import com.dima.shortener_service.entity.OutboxEvent;
import com.dima.shortener_service.repository.LinkRepository;
import com.dima.shortener_service.repository.OutboxEventRepository;
import com.dima.shortener_service.producer.LinkEventProducer;
import com.dima.shortener_service.client.AnalyticsClient;

import java.time.Instant;

@SpringBootTest
@TestPropertySource(properties = {
        "app.base-url=http://localhost:8080",
        "analytics.service-url=http://localhost:8081"
})
class LinkServiceTransactionalTest {

    @Autowired
    private LinkService linkService;

    @Autowired
    private LinkRepository linkRepository;

    @MockitoBean
    private LinkEventProducer linkEventProducer;

    @MockitoBean
    private AnalyticsClient analyticsClient;

    @MockitoSpyBean
    private OutboxEventRepository outboxEventRepository;

    @Test
    void redirect_rollback_noClicksAndNoOutboxEventSaved() {
        Link link = Link.builder()
                .originalUrl("https://example.com")
                .shortCode("test123")
                .clicks(0)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        linkRepository.save(link);

        doThrow(new RuntimeException("DB failure"))
                .when(outboxEventRepository).save(any(OutboxEvent.class));

        assertThatThrownBy(() ->
                linkService.publishLinkClickedEvent(
                        "test123", "https://example.com", "Mozilla/5.0"))
                .isInstanceOf(RuntimeException.class);

        Link reloaded = linkRepository.findByShortCode("test123").orElseThrow();
        assertThat(reloaded.getClicks()).isEqualTo(0);

        assertThat(outboxEventRepository.findAll()).isEmpty();
    }

    @AfterEach
    void cleanUp() {
        linkRepository.deleteAll();
    }
}