package com.site.digitalBook.controller;

import com.site.digitalBook.entity.Commande;
import com.site.digitalBook.entity.User;
import com.site.digitalBook.service.CommandeService;
import com.site.digitalBook.controller.payload.Payload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class CommandeController {

    private final CommandeService commandeService;

    @Autowired
    public CommandeController(CommandeService commandeService) {
        this.commandeService = commandeService;
    }

    @PostMapping(value = "/commande", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Payload> createCommande(@RequestBody Commande commande) {
        try {
            Commande savedCommande = commandeService.addCommande(commande);
            Payload payload = new Payload("Commande créée avec succès.", savedCommande);
            return new ResponseEntity<>(payload, HttpStatus.CREATED);
        } catch (Exception e) {
            Payload payload = new Payload("Erreur lors de la création de la commande : " + e.getMessage());
            return new ResponseEntity<>(payload, HttpStatus.BAD_REQUEST);
        }
    }


    // Endpoint pour récupérer toutes les commandes
    @GetMapping("/commandes")
    public ResponseEntity<Payload> getAllCommandes() {
        try {
            List<Commande> commandes = commandeService.getAllCommandes();
            if (commandes.isEmpty()) {
                Payload payload = new Payload("Aucune commande trouvée.");
                return new ResponseEntity<>(payload, HttpStatus.NO_CONTENT);
            }
            Payload payload = new Payload("Commandes récupérées avec succès.", commandes);
            return new ResponseEntity<>(payload, HttpStatus.OK);
        } catch (Exception e) {
            Payload payload = new Payload("Erreur lors de la récupération des commandes : " + e.getMessage());
            return new ResponseEntity<>(payload, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/commandes/{userId}")
    public ResponseEntity<Payload> getCommandesByUserId(@PathVariable Integer userId) {
        try {
            List<Commande> commandes = commandeService.findByUserId(userId);
            if (commandes.isEmpty()) {
                Payload payload = new Payload("Aucune commande trouvée pour l'utilisateur ID : " + userId);
                return new ResponseEntity<>(payload, HttpStatus.NO_CONTENT);
            }

            // Transformer les commandes en un format simplifié
            List<Map<String, Object>> commandeMaps = commandes.stream().map(commande -> {
                return Map.of(
                    "id", commande.getId(),
                    "user", Map.of("id", commande.getUser().getId()),
                    "prixTotal", commande.getPrixTotal(),
                    "methodePaiement", commande.getMethodePaiement(),
                    "dateCreation", commande.getDateCreation(),
                    "livreIds", commande.getLivreIds()
                );
            }).collect(Collectors.toList());

            Payload payload = new Payload("Commandes récupérées avec succès.", commandeMaps);
            return ResponseEntity.ok(payload);
        } catch (Exception e) {
            Payload payload = new Payload("Erreur lors de la récupération des commandes pour l'utilisateur : " + e.getMessage());
            return new ResponseEntity<>(payload, HttpStatus.BAD_REQUEST);
        }
    }
}
