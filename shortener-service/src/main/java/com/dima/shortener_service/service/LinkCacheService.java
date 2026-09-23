package com.dima.shortener_service.service;

import com.dima.shortener_service.dto.CachedLink;
import com.dima.shortener_service.entity.Link;
import com.dima.shortener_service.exception.LinkNotFoundException;
import com.dima.shortener_service.repository.LinkRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LinkCacheService {
    private final LinkRepository linkRepository;

    @Cacheable(value = "links", key = "#shortCode")
    public CachedLink get(String shortCode){
        Link link = linkRepository
                .findByShortCode(shortCode)
                .orElseThrow(() -> new LinkNotFoundException("Link not found"));

        return new CachedLink(link.getOriginalUrl(), link.getExpiresAt());
    }

    @Transactional
    @CacheEvict(value = "links", key = "#shortCode")
    public void deleteLink(String shortCode) {
        Link link = linkRepository
                .findByShortCode(shortCode)
                .orElseThrow(
                        () -> new LinkNotFoundException("Link not found")
                );

        log.info("Deleting link with short code: {}", shortCode);
        linkRepository.delete(link);
    }

}
