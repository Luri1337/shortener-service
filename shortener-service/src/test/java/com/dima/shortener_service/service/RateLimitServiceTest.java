package com.dima.shortener_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class RateLimitServiceTest {
    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void firstRequestShouldNotBeRateLimited() {
        String ipAddress = "192.168.0.0";

        when(valueOperations.increment("rate_limit:" + ipAddress)).thenReturn(1L);

        boolean isRateLimited = rateLimitService.isRateLimited(ipAddress);

        assertFalse(isRateLimited);
    }

    @Test
    void eleventhRequestShouldBeRateLimited() {
        String ipAddress = "192.168.0.0";

        when(valueOperations.increment("rate_limit:" + ipAddress)).thenReturn(11L);

        boolean isRateLimited = rateLimitService.isRateLimited(ipAddress);

        assertTrue(isRateLimited);
    }

    @Test
    void firstRequestShouldSetExpiration() {
        String ipAddress = "192.168.0.0";

        when(valueOperations.increment("rate_limit:" + ipAddress)).thenReturn(1L);

        rateLimitService.isRateLimited(ipAddress);

        verify(redisTemplate, times(1)).expire("rate_limit:" + ipAddress, Duration.ofSeconds(60));
    }

    @Test
    void subsequentRequestsShouldNotSetExpiration() {
        String ipAddress = "192.168.0.0";

        when(valueOperations.increment("rate_limit:" + ipAddress)).thenReturn(2L);

        rateLimitService.isRateLimited(ipAddress);

        verify(redisTemplate, times(0)).expire("rate_limit:" + ipAddress, Duration.ofSeconds(60));
    }

}
