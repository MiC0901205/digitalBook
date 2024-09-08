package com.site.digitalBook.service;

import com.site.digitalBook.entity.Panier;
import com.site.digitalBook.entity.Livre;
import com.site.digitalBook.entity.User;
import com.site.digitalBook.repository.CartRepository;
import com.site.digitalBook.repository.BookRepository;
import com.site.digitalBook.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    public CartService(CartRepository cartRepository, UserRepository userRepository, BookRepository bookRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    public Panier createCart(int userId) {
        logger.info("Création du panier pour l'utilisateur avec l'ID: {}", userId);
        User user = userRepository.findById(userId);

        if (user == null) {
            logger.error("Utilisateur non trouvé avec l'ID: {}", userId);
            throw new RuntimeException("Utilisateur non trouvé");
        }

        Panier cart = new Panier();
        cart.setUser(user);

        Panier savedCart = cartRepository.save(cart);
        logger.info("Panier créé et sauvegardé pour l'utilisateur avec l'ID: {}", userId);

        return savedCart;
    }

    public int getCartItemCount(int userId) {
        Panier cart = getCartByUserId(userId);
        int itemCount = cart != null ? cart.getLivres().size() : 0;
        logger.info("Nombre d'articles dans le panier de l'utilisateur avec l'ID {}: {}", userId, itemCount);
        return itemCount;
    }

    public Panier getCartByUserId(int userId) {
        Panier cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) {
            logger.warn("Panier non trouvé pour l'utilisateur avec l'ID: {}", userId);
        } else {
            logger.info("Panier récupéré pour l'utilisateur avec l'ID: {}", userId);
        }
        return cart;
    }

    public Panier addItemToCart(int userId, int bookId) throws Exception {
        logger.info("Ajout du livre avec l'ID {} au panier de l'utilisateur avec l'ID {}", bookId, userId);

        // Vérifier si l'utilisateur existe
        User user = userRepository.findById(userId);

        if (user == null) {
            logger.error("Utilisateur non trouvé avec l'ID: {}", userId);
            throw new IllegalArgumentException("Utilisateur non trouvé");
        }

        // Vérifier si le livre existe
        Livre book = bookRepository.findById(bookId)
                .orElseThrow(() -> {
                    logger.error("Livre non trouvé avec l'ID: {}", bookId);
                    return new IllegalArgumentException("Livre non trouvé");
                });

        // Rechercher le panier de l'utilisateur
        Panier cart = cartRepository.findByUserId(userId).orElse(new Panier());
        if (cart.getUser() == null) {
            cart.setUser(user);
        }

        // Ajouter le livre au panier
        cart.addBook(book);

        // Sauvegarder les changements dans le panier
        Panier updatedCart = cartRepository.save(cart);
        logger.info("Livre avec l'ID {} ajouté au panier de l'utilisateur avec l'ID {}", bookId, userId);

        return updatedCart;
    }

    public Panier removeItemFromCart(int userId, int bookId) {
        logger.info("Suppression du livre avec l'ID {} du panier de l'utilisateur avec l'ID {}", bookId, userId);
        Panier cart = getCartByUserId(userId);
        Livre book = bookRepository.findById(bookId)
                .orElseThrow(() -> {
                    logger.error("Livre non trouvé avec l'ID: {}", bookId);
                    return new RuntimeException("Livre non trouvé");
                });

        cart.getLivres().remove(book);
        Panier updatedCart = cartRepository.save(cart);
        logger.info("Livre avec l'ID {} supprimé du panier de l'utilisateur avec l'ID {}", bookId, userId);

        return updatedCart;
    }

    public void clearCart(int userId) {
        logger.info("Vidage du panier de l'utilisateur avec l'ID: {}", userId);
        Panier cart = getCartByUserId(userId);
        if (cart != null) {
            cart.getLivres().clear();
            cartRepository.save(cart);
            logger.info("Panier de l'utilisateur avec l'ID {} vidé avec succès", userId);
        } else {
            logger.warn("Panier non trouvé pour l'utilisateur avec l'ID: {}", userId);
        }
    }
}
