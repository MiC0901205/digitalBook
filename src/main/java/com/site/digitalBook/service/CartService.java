package com.site.digitalBook.service;

import com.site.digitalBook.entity.Panier;
import com.site.digitalBook.entity.Livre;
import com.site.digitalBook.entity.User;
import com.site.digitalBook.repository.CartRepository;
import com.site.digitalBook.repository.BookRepository; // Assurez-vous d'avoir ce repository
import com.site.digitalBook.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository; // Ajout du repository Livre

    public CartService(CartRepository cartRepository, UserRepository userRepository, BookRepository livreRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.bookRepository = livreRepository; // Initialisation du repository Livre
    }

    public Panier createCart(int userId) {
        User user = userRepository.findById(userId);
        
        if(user == null) {
        	throw new RuntimeException("User not found");
        }

        Panier cart = new Panier();
        cart.setUser(user);

        return cartRepository.save(cart);
    }

    public int getCartItemCount(int userId) {
        Panier cart = getCartByUserId(userId);
        return cart != null ? cart.getLivres().size() : 0;
    }

    public Panier getCartByUserId(int userId) {
        return cartRepository.findByUserId(userId)
            .orElse(null);
    }



    public Panier addItemToCart(int userId, int bookId) throws Exception {
        // Vérifier si l'utilisateur existe
        User user = userRepository.findById(userId);
        		
        if(user == null) {
        	throw new IllegalArgumentException("Utilisateur non trouvé");
        }

        // Vérifier si le livre existe
        Livre book = bookRepository.findById(bookId)
            .orElseThrow(() -> new IllegalArgumentException("Livre non trouvé"));

        // Rechercher le panier de l'utilisateur
        Panier cart = cartRepository.findByUserId(userId).orElse(null);

        // Si aucun panier n'est trouvé, en créer un nouveau
        if (cart == null) {
            cart = new Panier();
            cart.setUser(user);
        }

        // Ajouter le livre au panier
        cart.addBook(book);
        
        // Sauvegarder les changements dans le panier
        cartRepository.save(cart);

        return cart;
    }

    public Panier removeItemFromCart(int userId, int bookId) {
        Panier cart = getCartByUserId(userId);
        Livre livre = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        cart.getLivres().remove(livre);
        return cartRepository.save(cart);
    }

    public void clearCart(int userId) {
        Panier cart = getCartByUserId(userId);
        cart.getLivres().clear();
        cartRepository.save(cart);
    }
}
