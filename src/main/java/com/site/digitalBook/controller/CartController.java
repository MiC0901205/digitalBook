package com.site.digitalBook.controller;

import com.site.digitalBook.entity.Livre;
import com.site.digitalBook.entity.Panier;
import com.site.digitalBook.service.CartService;
import com.site.digitalBook.controller.payload.Payload;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/cart/create")
    public ResponseEntity<Payload> createCart(@RequestParam int userId) {
        try {
            Panier cart = cartService.createCart(userId);
            Payload payload = new Payload("Panier créé avec succès.", cart);
            return new ResponseEntity<>(payload, HttpStatus.CREATED);
        } catch (Exception e) {
            Payload payload = new Payload("Erreur lors de la création du panier : " + e.getMessage());
            return new ResponseEntity<>(payload, HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/cart/{userId}/count")
    public ResponseEntity<Payload> getCartItemCount(@PathVariable int userId) {
        try {
            // Appel du service pour obtenir le nombre d'articles
            int itemCount = cartService.getCartItemCount(userId);
            
            // Créer un Payload avec un message de succès et le nombre d'articles
            Payload payload = new Payload("Nombre d'articles récupéré avec succès.", itemCount);
            return ResponseEntity.ok(payload);
        } catch (Exception e) {
            // Créer un Payload avec un message d'erreur
            Payload payload = new Payload("Erreur lors de la récupération du nombre d'articles : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(payload);
        }
    }

    
    @GetMapping("/cart/{userId}/items")
    public ResponseEntity<Payload> getCartItems(@PathVariable int userId) {
        try {
            // Appel du service pour obtenir le panier
            Panier cart = cartService.getCartByUserId(userId);

            if (cart != null) {
                // Si le panier existe, retourner les livres
                List<Livre> livres = new ArrayList<>(cart.getLivres());
                Payload payload = new Payload("Éléments du panier récupérés avec succès.", livres);
                return ResponseEntity.ok(payload);
            } else {
                // Si le panier n'existe pas, retourner une erreur 404
                Payload payload = new Payload("Panier non trouvé pour l'utilisateur ID : " + userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(payload);
            }
        } catch (Exception e) {
            // Créer un Payload avec un message d'erreur
            Payload payload = new Payload("Erreur lors de la récupération des éléments du panier : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(payload);
        }
    }


    @PostMapping("/cart/add")
    public ResponseEntity<Payload> addItem(@RequestParam int userId, @RequestParam int bookId) {
        try {
            Panier cart = cartService.addItemToCart(userId, bookId);
            Payload payload = new Payload("Article ajouté au panier avec succès.", cart);
            return new ResponseEntity<>(payload, HttpStatus.OK);
        } catch (Exception e) {
            Payload payload = new Payload("Erreur lors de l'ajout de l'article au panier : " + e.getMessage());
            return new ResponseEntity<>(payload, HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/cart/remove")
    public ResponseEntity<Payload> removeItem(@RequestParam int userId, @RequestParam int bookId) {
        try {
            Panier cart = cartService.removeItemFromCart(userId, bookId);
            Payload payload = new Payload("Article retiré du panier avec succès.", cart);
            return new ResponseEntity<>(payload, HttpStatus.OK);
        } catch (Exception e) {
            Payload payload = new Payload("Erreur lors de la suppression de l'article du panier : " + e.getMessage());
            return new ResponseEntity<>(payload, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/cart/clear")
    public ResponseEntity<Payload> clearCart(@RequestParam int userId) {
        try {
            cartService.clearCart(userId);
            Payload payload = new Payload("Panier vidé avec succès.");
            return new ResponseEntity<>(payload, HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            Payload payload = new Payload("Erreur lors du vidage du panier : " + e.getMessage());
            return new ResponseEntity<>(payload, HttpStatus.BAD_REQUEST);
        }
    }
}
