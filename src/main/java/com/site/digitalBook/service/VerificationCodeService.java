package com.site.digitalBook.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class VerificationCodeService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationCodeService.class);
    private final static ConcurrentMap<String, ConfirmationCode> codeStore = new ConcurrentHashMap<>();

    public String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        byte[] code = new byte[6];
        random.nextBytes(code);
        String generatedCode = Base64.getUrlEncoder().withoutPadding().encodeToString(code);
        logger.info("Code de vérification généré : {}", generatedCode);
        return generatedCode;
    }

    public void saveCode(String email, String code) {
        codeStore.put(email, new ConfirmationCode(code, LocalDateTime.now().plusMinutes(10)));
        logger.info("Code de vérification sauvegardé pour l'email {} : {}", email, code);
    }

    public boolean validateCode(String email, String code) {
        ConfirmationCode storedCode = codeStore.get(email);
        if (storedCode != null) {
            if (storedCode.getCode().equals(code)) {
                if (storedCode.getExpiryDate().isAfter(LocalDateTime.now())) {
                    logger.info("Code de vérification valide pour l'email {} : {}", email, code);
                    return true;
                } else {
                    codeStore.remove(email);
                    logger.info("Code de vérification expiré pour l'email {} : {}", email, code);
                }
            } else {
                logger.info("Code de vérification invalide pour l'email {} : {}", email, code);
            }
        } else {
            logger.info("Aucun code de vérification trouvé pour l'email {}.", email);
        }
        return false;
    }

    private static class ConfirmationCode {
        private final String code;
        private final LocalDateTime expiryDate;

        public ConfirmationCode(String code, LocalDateTime expiryDate) {
            this.code = code;
            this.expiryDate = expiryDate;
        }

        public String getCode() {
            return code;
        }

        public LocalDateTime getExpiryDate() {
            return expiryDate;
        }

        @Override
        public String toString() {
            return "ConfirmationCode{" +
                    "code='" + code + '\'' +
                    ", expiryDate=" + expiryDate +
                    '}';
        }
    }
}
