package com.site.digitalBook.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

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
            logger.info("E-mail de confirmation envoyé à l'adresse : {}", to);
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'e-mail de confirmation à l'adresse : {}", to, e);
            throw e;
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
            logger.info("E-mail de réinitialisation de mot de passe envoyé à l'adresse : {}", to);
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'e-mail de réinitialisation de mot de passe à l'adresse : {}", to, e);
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
            logger.info("E-mail de confirmation de changement de mot de passe envoyé à l'adresse : {}", to);
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'e-mail de confirmation de changement de mot de passe à l'adresse : {}", to, e);
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
            logger.info("E-mail de confirmation de commande envoyé à l'adresse : {}", to);
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'e-mail de confirmation de commande à l'adresse : {}", to, e);
            throw e;
        }
    }
    
    public void sendSimpleEmail(String to, String subject, String messageBody) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(messageBody, true); // true pour le contenu HTML, false pour le texte brut

            mailSender.send(message);
            logger.info("E-mail simple envoyé à l'adresse : {} avec le sujet : {}", to, subject);
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'e-mail simple à l'adresse : {} avec le sujet : {}", to, subject, e);
            throw e;
        }
    }
}
