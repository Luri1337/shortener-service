package com.dima.analytics_service.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LinkClickedEvent {
    private String shortCode;
    private String originalUrl;
    private String clickedAt;
    private String userAgent;
    private String correlationId;
}
