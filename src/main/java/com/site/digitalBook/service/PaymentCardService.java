package com.site.digitalBook.service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.site.digitalBook.entity.CarteDePaiement;
import com.site.digitalBook.repository.PaymentCardRepository;
import com.site.digitalBook.util.KeyUtil;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Service
public class PaymentCardService {

    private static final String ALGORITHM = "AES";
    
    // Clé générée (charge-la ici)
    private static SecretKey secretKey;

    static {
        try {
            // Charge la clé à partir d'un stockage sécurisé ou génère-en une nouvelle
            secretKey = KeyUtil.generateKey();
            // Optionnel : Sauvegarde la clé sous forme de chaîne Base64
            String keyString = KeyUtil.getKeyAsString(secretKey);
            System.out.println("Clé générée et sauvegardée : " + keyString);
            // Sauvegarde la clé (dans un fichier, base de données, etc.)
        } catch (Exception e) {
            e.printStackTrace();
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
                String cardNumberStr = new String(decryptedCardNumber, "UTF-8");
                String cvvStr = new String(decryptedCvv, "UTF-8");

                // Affichez ou utilisez les chaînes
                System.out.println("Card Number (decrypted): " + cardNumberStr);
                System.out.println("CVV (decrypted): " + cvvStr);
                
                // Optionnel : mettez à jour les valeurs dans l'objet si nécessaire
                card.setCardNumber(decryptedCardNumber); // Rétablir les valeurs décryptées
                card.setCvv(decryptedCvv); // Rétablir les valeurs décryptées

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return cards;
    }


    public CarteDePaiement addPaymentCard(CarteDePaiement card) {
        // Vérification des champs requis
        if (card.getCardNumber() == null || card.getCardNumber().length == 0) {
            throw new IllegalArgumentException("Le numéro de carte est requis.");
        }
        if (card.getCvv() == null || card.getCvv().length == 0) {
            throw new IllegalArgumentException("Le CVV est requis.");
        }
        if (card.getExpiryDate() == null || card.getExpiryDate().trim().isEmpty()) {
            throw new IllegalArgumentException("La date d'expiration est requise.");
        }

        return paymentCardRepository.save(card);
    }



    public void deletePaymentCard(int cardId) {
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
