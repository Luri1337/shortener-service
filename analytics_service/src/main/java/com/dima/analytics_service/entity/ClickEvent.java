package com.dima.analytics_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
@Table(name = "click_events")
public class ClickEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "click_seq")
    @SequenceGenerator(name = "click_seq", sequenceName = "click_seq", allocationSize = 50)
    @Column(name = "id")
    private Long id;

    @Column(name = "short_code", nullable = false)
    private String shortCode;

    @Column(name = "original_url", nullable = false)
    private String originalUrl;

    @Column(name = "clicked_at", nullable = false, updatable = false)
    private Instant clickedAt;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "correlation_id", nullable = false)
    private String correlationId;
}
