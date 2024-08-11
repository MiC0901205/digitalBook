package com.site.digitalBook.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class VerificationCodeStorageService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String CODE_KEY_PREFIX = "verification_code:";

    public void storeVerificationCode(String email, String code) {
        redisTemplate.opsForValue().set(CODE_KEY_PREFIX + email, code, Duration.ofMinutes(10));
    }

    public String getVerificationCode(String email) {
        return redisTemplate.opsForValue().get(CODE_KEY_PREFIX + email);
    }

    public void deleteVerificationCode(String email) {
        redisTemplate.delete(CODE_KEY_PREFIX + email);
    }
}
