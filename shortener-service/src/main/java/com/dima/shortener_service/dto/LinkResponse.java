package com.dima.shortener_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class LinkResponse {
    private String shortUrl;
    private String shortCode;
    private Instant expiresAt;
}
