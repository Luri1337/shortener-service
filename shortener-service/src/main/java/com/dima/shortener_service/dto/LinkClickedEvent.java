package com.dima.shortener_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
