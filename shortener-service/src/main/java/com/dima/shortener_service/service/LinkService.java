package com.dima.shortener_service.service;


import com.dima.shortener_service.dto.*;
import com.dima.shortener_service.entity.Link;
import com.dima.shortener_service.exception.LinkExpiredException;
import com.dima.shortener_service.exception.LinkNotFoundException;
import com.dima.shortener_service.producer.LinkEventProducer;
import com.dima.shortener_service.repository.LinkRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
public class LinkService {
    private final LinkRepository linkRepository;

    private final LinkEventProducer linkEventProducer;

    @Value("${app.base-url}")
    private String baseUrl;

    public LinkService(LinkRepository linkRepository, LinkEventProducer linkEventProducer) {
        this.linkRepository = linkRepository;
        this.linkEventProducer = linkEventProducer;
    }

    public LinkResponse createLink(CreateLinkRequest request) {
        Link link = Link.builder()
                .originalUrl(request.getOriginalUrl())
                .expiresAt(countDownExpiration(request.getDaysToExpire()))
                .shortCode(generateShortCode())
                .build();

        linkRepository.save(link);

        LinkResponse response = new LinkResponse();
        response.setShortUrl(baseUrl + "/r/" + link.getShortCode());
        response.setShortCode(link.getShortCode());
        response.setExpiresAt(link.getExpiresAt());
        return response;
    }

    @Cacheable(value = "links", key = "#shortCode")
    public String getOriginalUrl(String shortCode) {
        Link link = linkRepository
                .findByShortCode(shortCode)
                .orElseThrow(() -> new LinkNotFoundException("Link not found"));

        if (link.isExpired()) {
            throw new LinkExpiredException("Link has expired");
        }

        return link.getOriginalUrl();
    }

    public void publishLinkClickedEvent(String shortCode, String originalUrl, String userAgent) {
        linkEventProducer.produceLinkEvent(
                new LinkClickedEvent(
                        shortCode,
                        originalUrl,
                        Instant.now().toString(), userAgent,
                        UUID.randomUUID().toString())
        );
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
        response.setShortUrl(baseUrl + "/" + link.getShortCode());

        return response;
    }

    @Transactional
    @CacheEvict(value = "links", key = "#shortCode")
    public void deleteLink(String shortCode) {
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
}
