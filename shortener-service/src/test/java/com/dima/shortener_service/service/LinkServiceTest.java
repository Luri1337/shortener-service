package com.dima.shortener_service.service;

import com.dima.shortener_service.dto.CreateLinkRequest;
import com.dima.shortener_service.dto.LinkInfoResponse;
import com.dima.shortener_service.dto.LinkResponse;
import com.dima.shortener_service.entity.Link;
import com.dima.shortener_service.exception.LinkNotFoundException;
import com.dima.shortener_service.repository.LinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LinkServiceTest {
    @Mock
    private LinkRepository linkRepository;

    @InjectMocks
    private LinkService linkService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(linkService, "baseUrl", "http://localhost:8080");
    }

    @Test
    void createLink_shouldSaveAndReturnCorrectResponse() {
        CreateLinkRequest request = new CreateLinkRequest();
        request.setOriginalUrl("https://google.com");
        request.setDaysToExpire(7);

        when(linkRepository.existsByShortCode(anyString())).thenReturn(false);
        when(linkRepository.save(any(Link.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LinkResponse response = linkService.createLink(request);

        assertNotNull(response);
        assertNotNull(response.getShortCode());
        assertEquals(8, response.getShortCode().length());
        assertTrue(response.getShortUrl().startsWith("http://localhost:8080/"));
        assertNotNull(response.getExpiresAt());
        verify(linkRepository, times(1)).save(any(Link.class));
    }

    @Test
    void getLinkInfo_shouldReturnLinkInfo_whenShortCodeExists() {
        String shortCode = "abc12345";
        Link link = new Link();
        link.setShortCode(shortCode);
        link.setOriginalUrl("https://google.com");

        when(linkRepository.findByShortCode(shortCode)).thenReturn(Optional.of(link));

        LinkInfoResponse response = linkService.getLinkInfo(shortCode);

        assertNotNull(response);
        assertEquals(shortCode, response.getShortCode());
        assertEquals("https://google.com", response.getOriginalUrl());
    }

    @Test
    void getLinkInfo_shouldThrowException_whenShortCodeDoesNotExist() {
        String shortCode = "nonexistent";

        when(linkRepository.findByShortCode(shortCode)).thenReturn(Optional.empty());

        assertThrows(LinkNotFoundException.class, () -> linkService.getLinkInfo(shortCode));
    }
}
