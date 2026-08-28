package com.dima.shortener_service.controller;

import com.dima.shortener_service.dto.CreateLinkRequest;
import com.dima.shortener_service.dto.LinkInfoResponse;
import com.dima.shortener_service.dto.LinkResponse;
import com.dima.shortener_service.service.LinkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
public class LinkController {

    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @PostMapping("/api/links")
    public ResponseEntity<LinkResponse> createLink(@Valid @RequestBody CreateLinkRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(linkService.createLink(request));
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable String shortCode) {
        String originalUrl = linkService.getOriginalUrl(shortCode);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    @GetMapping("/api/links/{shortCode}")
    public ResponseEntity<LinkInfoResponse> getLinkInfo(@PathVariable String shortCode) {
        return ResponseEntity.ok(linkService.getLinkInfo(shortCode));
    }

    @DeleteMapping("/api/links/{shortCode}")
    public ResponseEntity<Void> deleteLink(@PathVariable String shortCode) {
        linkService.deleteLink(shortCode);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/api/links/{shortCode}/stats")
    public ResponseEntity<Map<String, Long>> getLinkStats(@PathVariable String shortCode) {
        long clicks = linkService.getClickCount(shortCode);
        return ResponseEntity.ok(Map.of("Clicks", clicks));
    }
}
