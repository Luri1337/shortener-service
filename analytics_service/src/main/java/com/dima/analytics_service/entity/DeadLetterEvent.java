package com.dima.analytics_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
@Table(name = "dead_letter_events")
public class DeadLetterEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "payload", nullable = false)
    private String payload;

    @Column(name = "error_message", nullable = false)
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "received_at")
    private Instant receivedAt;
}
