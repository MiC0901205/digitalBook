package com.site.digitalBook.service;

import com.site.digitalBook.entity.Commande;
import com.site.digitalBook.entity.Livre;
import com.site.digitalBook.entity.User;
import com.site.digitalBook.repository.CommandeRepository;
import com.site.digitalBook.repository.UserRepository;
import com.site.digitalBook.repository.BookRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public Commande addCommande(Commande commande) {
        if (commande.getUser() == null) {
            throw new IllegalArgumentException("User must not be null");
        }

        // Récupérer l'utilisateur de la base de données pour s'assurer qu'il a un email
        User user = userRepository.findById(commande.getUser().getId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getEmail() == null) {
            throw new IllegalArgumentException("User email must not be null");
        }

        commande.setUser(user); // Mettre à jour l'objet commande avec l'utilisateur complet

        Commande savedCommande = commandeRepository.save(commande);

        // Envoyer l'e-mail de confirmation avec les PDFs téléchargés
        try {
            emailService.sendOrderConfirmationEmail(user.getEmail(), savedCommande.getId().toString());
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return savedCommande;
    }

    public Commande findById(Integer id) {
        return commandeRepository.findById(id).orElse(null);
    }

    public List<Commande> getAllCommandes() {
        return commandeRepository.findAll();
    }

    public List<Commande> findByUserId(Integer userId) {
        return commandeRepository.findByUserId(userId);
    }
}
