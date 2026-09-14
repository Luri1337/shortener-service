package com.dima.shortener_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@EntityListeners(AuditingEntityListener.class)

@Entity
@Table(name = "links")
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "link_seq")
    @SequenceGenerator(name = "link_seq", sequenceName = "link_seq", allocationSize = 50)
    @Column(name = "id")
    private Long id;

    @Column(name = "short_code", nullable = false, unique = true)
    private String shortCode;

    @Column(name = "original_url", nullable = false)
    private String originalUrl;

    @Column(name = "clicks", nullable = false)
    private long clicks;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
}
