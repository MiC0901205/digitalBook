package com.site.digitalBook.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
public class EmailServiceTest {

    @Autowired
    private JavaMailSender mailSender;

    @Test
    public void testSendEmail() {
        String to = "test@example.com";
        String subject = "Test Email";
        String text = "Ceci est un email de test.";

        assertDoesNotThrow(() -> {
            try {
                sendTestEmail(to, subject, text);
            } catch (MessagingException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    private void sendTestEmail(String to, String subject, String text) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, false);

        mailSender.send(message);
    }
}
