package com.kacho.my_ocpp.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    private final StringRedisTemplate redisTemplate;

    public RedisService(StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = stringRedisTemplate;
    }

    public void saveSession(String sessionId, String serialNumber, long ttlMinutes) {
        redisTemplate.opsForValue().set(sessionId, serialNumber, ttlMinutes, TimeUnit.MINUTES);
    }

    public String getSerialNumber(String sessionId) {
        return redisTemplate.opsForValue().get(sessionId);
    }



    public boolean sessionExists(String sessionId) {
        return redisTemplate.hasKey(sessionId);
    }

    public void refreshSessionTTL(String sessionId, long ttlMinutes) {
        redisTemplate.expire(sessionId, ttlMinutes, TimeUnit.MINUTES);
    }

    public void deleteSession(String sessionId) {
        redisTemplate.delete(sessionId);
    }
}
