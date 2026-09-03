package com.dima.shortener_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class RateLimitService {
    private final RedisTemplate<String, String> redisTemplate;

    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final int WINDOW_SIZE = 60;

    public RateLimitService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isRateLimited(String ipAddress) {
        String key = "rate_limit:" + ipAddress;

        redisTemplate.opsForValue().setIfAbsent(key, "0", Duration.ofSeconds(WINDOW_SIZE));

        Long currentCount = redisTemplate.opsForValue().increment(key);

        return currentCount > MAX_REQUESTS_PER_MINUTE;
    }


}
