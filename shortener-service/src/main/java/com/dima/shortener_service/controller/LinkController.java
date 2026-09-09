package com.dima.shortener_service.controller;

import com.dima.shortener_service.client.AnalyticsClient;
import com.dima.shortener_service.dto.AnalyticsResponse;
import com.dima.shortener_service.dto.CreateLinkRequest;
import com.dima.shortener_service.dto.LinkInfoResponse;
import com.dima.shortener_service.dto.LinkResponse;
import com.dima.shortener_service.service.LinkService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Slf4j
@RestController
public class LinkController {

    private final LinkService linkService;

    private final AnalyticsClient analyticsClient;

    public LinkController(LinkService linkService, AnalyticsClient analyticsClient) {
        this.linkService = linkService;
        this.analyticsClient = analyticsClient;
    }

    @PostMapping("/api/links")
    public ResponseEntity<LinkResponse> createLink(@Valid @RequestBody CreateLinkRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(linkService.createLink(request));
    }

    @GetMapping("/r/{shortCode}")
    public ResponseEntity<Void> redirectToOriginalUrl(
            @PathVariable String shortCode,
            @RequestHeader(value = "User-Agent", defaultValue = "unknown") String userAgent) {

        String originalUrl = linkService.getOriginalUrl(shortCode);

        log.info("Redirecting shortcode: {} to: {}", shortCode, originalUrl);

        linkService.publishLinkClickedEvent(shortCode, originalUrl, userAgent);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    @GetMapping("/api/links/{shortCode}")
    public ResponseEntity<LinkInfoResponse> getLinkInfo(@PathVariable String shortCode) {
        return ResponseEntity.ok(linkService.getLinkInfo(shortCode));
    }

    @GetMapping("/api/links/{shortCode}/analytics")
    public ResponseEntity<AnalyticsResponse> getLinkAnalytics(@PathVariable String shortCode) {
        return ResponseEntity.ok(analyticsClient.getLinkAnalytics(shortCode));
    }

    @DeleteMapping("/api/links/{shortCode}")
    public ResponseEntity<Void> deleteLink(@PathVariable String shortCode) {
        linkService.deleteLink(shortCode);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
