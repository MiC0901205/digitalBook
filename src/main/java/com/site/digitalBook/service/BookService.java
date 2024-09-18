package com.site.digitalBook.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.site.digitalBook.entity.Livre;
import com.site.digitalBook.repository.BookRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Livre> getAllBooks() {
        logger.info("Récupération de tous les livres");
        return bookRepository.findAll();
    }

    public Livre saveBook(Livre book) {
        logger.info("Enregistrement du livre avec l'ID : {}", book.getId());
        return bookRepository.save(book);
    }

    public Livre getBookById(int id) {
        logger.info("Récupération du livre avec l'ID : {}", id);
        return bookRepository.findById(id).orElse(null);
    }

    // Nouvelle méthode pour récupérer les livres par catégorie
    public List<Livre> getBooksByCategory(String categoryName) {
        logger.info("Récupération des livres par catégorie : {}", categoryName);
        return bookRepository.findByCategoriesName(categoryName);
    }
    
    public Livre findById(Integer id) {
        logger.info("Recherche du livre par ID : {}", id);
        return bookRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Livre non trouvé avec l'ID : {}", id);
                    return new RuntimeException("Livre non trouvé avec l'ID " + id);
                });
    }
    
    public Livre updateBook(int id, double remise) {
        logger.info("Mise à jour de la remise du livre avec l'ID : {} à la valeur : {}", id, remise);
        Optional<Livre> optionalLivre = bookRepository.findById(id);
        if (optionalLivre.isPresent()) {
            Livre livre = optionalLivre.get();
            livre.setRemise(remise);
            Livre updatedLivre = bookRepository.save(livre);
            logger.info("Remise mise à jour avec succès pour le livre avec l'ID : {}", id);
            return updatedLivre;
        }
        logger.warn("Livre non trouvé avec l'ID : {}", id);
        return null;
    }
   
    public Livre addBook(Livre newBook) {
        logger.info("Ajout du nouveau livre avec l'ID : {}", newBook.getId());
        return bookRepository.save(newBook);
    }
}
