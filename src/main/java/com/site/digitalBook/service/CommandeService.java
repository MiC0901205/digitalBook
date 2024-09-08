package com.site.digitalBook.service;

import com.site.digitalBook.entity.Commande;
import com.site.digitalBook.entity.User;
import com.site.digitalBook.repository.CommandeRepository;
import com.site.digitalBook.repository.UserRepository;
import com.site.digitalBook.repository.BookRepository;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommandeService {

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(CommandeService.class);

    public Commande addCommande(Commande commande) {
        if (commande.getUser() == null) {
            logger.error("Erreur lors de l'ajout de la commande : l'utilisateur est nul.");
            throw new IllegalArgumentException("L'utilisateur ne doit pas être nul");
        }

        // Récupérer l'utilisateur de la base de données pour s'assurer qu'il a un email
        User user = userRepository.findById(commande.getUser().getId())
            .orElseThrow(() -> {
                logger.error("Erreur lors de l'ajout de la commande : utilisateur non trouvé avec l'ID: {}", commande.getUser().getId());
                return new IllegalArgumentException("Utilisateur non trouvé");
            });

        if (user.getEmail() == null) {
            logger.error("Erreur lors de l'ajout de la commande : l'email de l'utilisateur est nul.");
            throw new IllegalArgumentException("L'email de l'utilisateur ne doit pas être nul");
        }

        commande.setUser(user); // Mettre à jour l'objet commande avec l'utilisateur complet

        Commande savedCommande = commandeRepository.save(commande);
        logger.info("Commande ajoutée avec succès. ID: {}", savedCommande.getId());

        // Envoyer l'e-mail de confirmation avec les PDFs téléchargés
        try {
            emailService.sendOrderConfirmationEmail(user.getEmail(), savedCommande.getId().toString());
            logger.info("E-mail de confirmation envoyé à l'adresse: {}", user.getEmail());
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'e-mail de confirmation pour la commande ID: {}", savedCommande.getId(), e);
        }

        return savedCommande;
    }

    public Commande findById(Integer id) {
        Commande commande = commandeRepository.findById(id).orElse(null);
        if (commande == null) {
            logger.warn("Commande non trouvée avec l'ID: {}", id);
        } else {
            logger.info("Commande trouvée avec l'ID: {}", id);
        }
        return commande;
    }

    public List<Commande> getAllCommandes() {
        logger.info("Récupération de toutes les commandes.");
        List<Commande> commandes = commandeRepository.findAll();
        logger.info("Nombre de commandes récupérées: {}", commandes.size());
        return commandes;
    }

    public List<Commande> findByUserId(Integer userId) {
        logger.info("Récupération des commandes pour l'utilisateur ID: {}", userId);
        List<Commande> commandes = commandeRepository.findByUserId(userId);
        logger.info("Nombre de commandes récupérées pour l'utilisateur ID {}: {}", userId, commandes.size());
        return commandes;
    }
}
