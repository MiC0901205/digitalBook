package com.site.digitalBook.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.site.digitalBook.entity.Livre;
import com.site.digitalBook.repository.BookRepository;
import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Livre> getAllBooks() {
        return bookRepository.findAll();
    }

    public Livre saveBook(Livre book) {
        return bookRepository.save(book);
    }

    public Livre getBookById(int id) {
        return bookRepository.findById(id).orElse(null);
    }

    // Nouvelle méthode pour récupérer les livres par catégorie
    public List<Livre> getBooksByCategory(String categoryName) {
        return bookRepository.findByCategoriesName(categoryName);
    }
    
    public Livre findById(Integer id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livre not found with id " + id));
    }
    
    public Livre ajouterLivre(Livre livre) {
        return bookRepository.save(livre);
    }
}
