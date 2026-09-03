package com.dima.shortener_service.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

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
        Long currentCount = redisTemplate.opsForValue().increment(key);
        if (currentCount == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(WINDOW_SIZE));
        }
        return currentCount > MAX_REQUESTS_PER_MINUTE;
    }


}
