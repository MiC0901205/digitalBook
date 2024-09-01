package com.site.digitalBook.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.activation.FileDataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.File;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.time.LocalDateTime;


@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final ConcurrentMap<String, ConfirmationCode> codeStore = new ConcurrentHashMap<>();

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendConfirmationEmail(String to, String code) throws MessagingException {
        String subject = "Votre Code de Confirmation";
        String text = "Votre code de confirmation est : " + code;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw e;
        }
    }

    public String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        byte[] code = new byte[6]; // 6 bytes = 48 bits = 8 characters base64
        random.nextBytes(code);
        String generatedCode = Base64.getUrlEncoder().withoutPadding().encodeToString(code);
        return generatedCode;
    }

    public void saveCode(String email, String code) {
        codeStore.put(email, new ConfirmationCode(code, LocalDateTime.now().plusMinutes(10)));
    }

    public boolean validateCode(String email, String code) {
        ConfirmationCode storedCode = codeStore.get(email);

        if (storedCode != null && storedCode.getCode().equals(code)) {
            if (storedCode.getExpiryDate().isAfter(LocalDateTime.now())) {
                return true;
            } else {
                codeStore.remove(email);
            }
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

    public void sendResetPasswordEmail(String to, String resetLink) throws MessagingException {
        String subject = "Réinitialisation de votre mot de passe";
        String text = "Voici votre lien de réinitialisation : " + resetLink;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw e;
        }
    }
    
    public void sendPasswordChangeConfirmationEmail(String to) throws MessagingException {
        String subject = "Changement de mot de passe réussi";
        String text = "Votre mot de passe a été changé avec succès.";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw e;
        }
    }
    
    public void sendOrderConfirmationEmail(String to, String orderId) throws MessagingException {
        String subject = "Confirmation de votre commande";
        String text = "Votre commande avec l'ID " + orderId + " a été enregistrée avec succès. " +
                      "Vous pouvez consulter votre historique d'achat pour plus de détails.";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true); 

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw e;
        }
    }

}
