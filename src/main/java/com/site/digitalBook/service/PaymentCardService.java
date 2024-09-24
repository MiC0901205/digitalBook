package com.site.digitalBook.service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.site.digitalBook.entity.CarteDePaiement;
import com.site.digitalBook.repository.PaymentCardRepository;
import com.site.digitalBook.util.KeyUtil;

import java.nio.charset.StandardCharsets;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PaymentCardService {

    private static final String ALGORITHM = "AES";
    private static final Logger logger = LoggerFactory.getLogger(PaymentCardService.class);
    
    // Clé générée (charge-la ici)
    private static SecretKey secretKey;

    static {
        try {
            // Charge la clé à partir d'un stockage sécurisé ou génère-en une nouvelle
            secretKey = KeyUtil.generateKey();
            // Optionnel : Sauvegarde la clé sous forme de chaîne Base64
            String keyString = KeyUtil.getKeyAsString(secretKey);
            logger.info("Clé générée et sauvegardée : {}", keyString);
            // Sauvegarde la clé (dans un fichier, base de données, etc.)
        } catch (Exception e) {
            logger.error("Erreur lors de la génération de la clé : {}", e.getMessage(), e);
        }
    }

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    public List<CarteDePaiement> getPaymentCardsByUserId(int userId) {
        List<CarteDePaiement> cards = paymentCardRepository.findByUserId(userId);

        cards.forEach(card -> {
            try {
                byte[] decryptedCardNumber = decrypt(card.getCardNumber()); // Déchiffre le numéro de carte
                byte[] decryptedCvv = decrypt(card.getCvv());               // Déchiffre le CVV

                // Convertir les octets en chaînes pour l'affichage
                String cardNumberStr = new String(decryptedCardNumber, StandardCharsets.UTF_8);
                String cvvStr = new String(decryptedCvv, StandardCharsets.UTF_8);

                // Utilisation des loggers
                logger.info("Card Number (decrypted): {}", cardNumberStr);
                logger.info("CVV (decrypted): {}", cvvStr);
                
                // Optionnel : mettez à jour les valeurs dans l'objet si nécessaire
                card.setCardNumber(decryptedCardNumber); // Rétablir les valeurs décryptées
                card.setCvv(decryptedCvv); // Rétablir les valeurs décryptées

            } catch (Exception e) {
                logger.error("Erreur lors du déchiffrement de la carte : {}", e.getMessage(), e);
            }
        });
        return cards;
    }


    public CarteDePaiement addPaymentCard(CarteDePaiement card) {
        // Vérification des champs requis
        if (card.getCardNumber() == null || card.getCardNumber().length == 0) {
            logger.error("Le numéro de carte est requis.");
            throw new IllegalArgumentException("Le numéro de carte est requis.");
        }
        if (card.getCvv() == null || card.getCvv().length == 0) {
            logger.error("Le CVV est requis.");
            throw new IllegalArgumentException("Le CVV est requis.");
        }
        if (card.getExpiryDate() == null || card.getExpiryDate().trim().isEmpty()) {
            logger.error("La date d'expiration est requise.");
            throw new IllegalArgumentException("La date d'expiration est requise.");
        }

        logger.info("Ajout de la carte de paiement : {}", card);
        return paymentCardRepository.save(card);
    }

    public void deletePaymentCard(int cardId) {
        logger.info("Suppression de la carte de paiement avec l'ID : {}", cardId);
        paymentCardRepository.deleteById(cardId);
    }

    private byte[] encrypt(byte[] data) throws Exception {
        SecretKeySpec key = new SecretKeySpec(secretKey.getEncoded(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    private byte[] decrypt(byte[] encryptedData) throws Exception {
        SecretKeySpec key = new SecretKeySpec(secretKey.getEncoded(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
    }
}
