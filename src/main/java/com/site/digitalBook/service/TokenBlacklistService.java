package com.site.digitalBook.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class TokenBlacklistService {

    private final Set<String> blacklistedTokens = new HashSet<>();
    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);

    public void addToBlacklist(String token) {
        blacklistedTokens.add(token);
        logger.info("Jeton ajouté à la liste noire : {}", token);
    }

    public boolean isTokenBlacklisted(String token) {
        boolean isBlacklisted = blacklistedTokens.contains(token);
        if (isBlacklisted) {
            logger.info("Jeton trouvé dans la liste noire : {}", token);
        } else {
            logger.info("Jeton non trouvé dans la liste noire : {}", token);
        }
        return isBlacklisted;
    }
}
