package com.site.digitalBook.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class VerificationCodeStorageService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationCodeStorageService.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String CODE_KEY_PREFIX = "verification_code:";

    public void storeVerificationCode(String email, String code) {
        redisTemplate.opsForValue().set(CODE_KEY_PREFIX + email, code, Duration.ofMinutes(10));
        logger.info("Code de vérification stocké pour l'email {} : {}", email, code);
    }

    public String getVerificationCode(String email) {
        String code = redisTemplate.opsForValue().get(CODE_KEY_PREFIX + email);
        if (code != null) {
            logger.info("Code de vérification récupéré pour l'email {} : {}", email, code);
        } else {
            logger.info("Aucun code de vérification trouvé pour l'email {}.", email);
        }
        return code;
    }

    public void deleteVerificationCode(String email) {
        redisTemplate.delete(CODE_KEY_PREFIX + email);
        logger.info("Code de vérification supprimé pour l'email {}.", email);
    }
}
