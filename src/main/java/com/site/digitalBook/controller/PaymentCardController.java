package com.site.digitalBook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.site.digitalBook.controller.payload.Payload;
import com.site.digitalBook.entity.CarteDePaiement;
import com.site.digitalBook.service.PaymentCardService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PaymentCardController {

    @Autowired
    private PaymentCardService paymentCardService;

    // Récupère les cartes de paiement pour un utilisateur donné
    @GetMapping("/payment-cards/{userId}")
    public ResponseEntity<Payload> getPaymentCardsByUserId(@PathVariable Integer userId) {
        List<CarteDePaiement> cards = paymentCardService.getPaymentCardsByUserId(userId);
        return ResponseEntity.ok(new Payload("Cartes de paiement récupérées avec succès.", cards));
    }

    @PostMapping("/payment-cards")
    public ResponseEntity<Payload> addPaymentCard(@RequestBody CarteDePaiement card) {
        try {
            // Vérifier les champs requis dans le service ou ici
            if (card.getCardNumber() == null || card.getCardNumber().length == 0) {
                throw new IllegalArgumentException("Le numéro de carte est requis.");
            }
            if (card.getCvv() == null || card.getCvv().length == 0) {
                throw new IllegalArgumentException("Le CVV est requis.");
            }
            if (card.getExpiryDate() == null || card.getExpiryDate().trim().isEmpty()) {
                throw new IllegalArgumentException("La date d'expiration est requise.");
            }

            CarteDePaiement savedCard = paymentCardService.addPaymentCard(card);
            Payload payload = new Payload("Carte de paiement ajoutée avec succès.", savedCard);
            return new ResponseEntity<>(payload, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Renvoyer un payload d'erreur avec un message explicite
            Payload payload = new Payload("Erreur lors de l'ajout de la carte : " + e.getMessage());
            return new ResponseEntity<>(payload, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // Renvoyer un message d'erreur générique
            Payload payload = new Payload("Une erreur est survenue lors de l'ajout de la carte : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(payload);
        }
    }
    
    // Supprime une carte de paiement par son ID
    @DeleteMapping("/payment-cards/{cardId}")
    public ResponseEntity<Void> deletePaymentCard(@PathVariable Integer cardId) {
        paymentCardService.deletePaymentCard(cardId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
