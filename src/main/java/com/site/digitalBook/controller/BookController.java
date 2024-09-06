package com.site.digitalBook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.site.digitalBook.entity.Livre;
import com.site.digitalBook.service.BookService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/books")
    public ResponseEntity<List<Livre>> getAllBooks() {
        try {
            List<Livre> books = bookService.getAllBooks();
            if (books.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<Livre> getBookById(@PathVariable int id) {
        try {
            Livre book = bookService.getBookById(id);
            if (book != null) {
                return new ResponseEntity<>(book, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/books/category/{categoryName}")
    public ResponseEntity<List<Livre>> getBooksByCategory(@PathVariable String categoryName) {
        try {
            List<Livre> books = bookService.getBooksByCategory(categoryName);
            if (books.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    @PutMapping("/book/{id}")
    public ResponseEntity<Livre> updateBook(@PathVariable int id, @RequestBody Map<String, Object> updates) {
        try {
            if (updates.containsKey("remise")) {
                double remise = Double.parseDouble(updates.get("remise").toString());
                Livre updatedBook = bookService.updateBook(id, remise);
                if (updatedBook != null) {
                    return new ResponseEntity<>(updatedBook, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    @PostMapping(value = "/book", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Livre> addBook(@RequestBody Livre newBook) {
        try {
            // Validation des données du livre
            if (newBook.getTitre() == null || newBook.getAuteur() == null || newBook.getPrix() <= 0) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            // Appel du service pour ajouter le livre
            Livre savedBook = bookService.addBook(newBook);
            return new ResponseEntity<>(savedBook, HttpStatus.CREATED);
        } catch (Exception e) {
            // Affiche l'exception si quelque chose se passe mal
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    
    @PostMapping("/test")
    public ResponseEntity<String> test(@RequestBody Livre livre) {
        return new ResponseEntity<>("Received", HttpStatus.OK);
    }


}
