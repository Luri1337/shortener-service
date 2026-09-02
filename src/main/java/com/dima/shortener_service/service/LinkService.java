package com.dima.shortener_service.service;


import com.dima.shortener_service.dto.CreateLinkRequest;
import com.dima.shortener_service.dto.LinkInfoResponse;
import com.dima.shortener_service.dto.LinkResponse;
import com.dima.shortener_service.entity.Link;
import com.dima.shortener_service.exception.LinkExpiredException;
import com.dima.shortener_service.exception.LinkNotFoundException;
import com.dima.shortener_service.repository.LinkRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class LinkService {
    private final LinkRepository linkRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    public LinkService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    public LinkResponse createLink(CreateLinkRequest request) {
        Link link = new Link();
        link.setOriginalUrl(request.getOriginalUrl());
        link.setExpiresAt(countDownExpiration(request.getDaysToExpire()));
        link.setShortCode(generateShortCode());

        linkRepository.save(link);

        LinkResponse response = new LinkResponse();
        response.setShortUrl(baseUrl + "/" + link.getShortCode());
        response.setShortCode(link.getShortCode());
        response.setExpiresAt(link.getExpiresAt());
        return response;
    }

    public String getOriginalUrl(String shortCode) {
        Link link = linkRepository
                .findByShortCode(shortCode)
                .orElseThrow(() -> new LinkNotFoundException("Link not found"));

        if (link.isExpired()) {
            throw new LinkExpiredException("Link has expired");
        }

        link.setClicks(link.getClicks() + 1);
        linkRepository.save(link);

        return link.getOriginalUrl();
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

    public long getClickCount(String shortCode) {
        Link link = linkRepository.findByShortCode(shortCode)
                .orElseThrow(
                        () -> new LinkNotFoundException("Link not found")
                );
        return link.getClicks();
    }

    @Transactional
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
