package com.site.digitalBook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.site.digitalBook.entity.Livre;
import com.site.digitalBook.service.BookService;
import com.site.digitalBook.controller.payload.Payload;

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
    public ResponseEntity<Payload> getAllBooks() {
        try {
            List<Livre> books = bookService.getAllBooks();
            if (books.isEmpty()) {
                return new ResponseEntity<>(new Payload("Aucun livre trouvé.", books), HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(new Payload("Livres récupérés avec succès.", books), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new Payload("Erreur lors de la récupération des livres : " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<Payload> getBookById(@PathVariable int id) {
        try {
            Livre book = bookService.getBookById(id);
            if (book != null) {
                return new ResponseEntity<>(new Payload("Livre récupéré avec succès.", book), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new Payload("Livre non trouvé pour l'ID : " + id), HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(new Payload("Erreur lors de la récupération du livre : " + e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/books/category/{categoryName}")
    public ResponseEntity<Payload> getBooksByCategory(@PathVariable String categoryName) {
        try {
            List<Livre> books = bookService.getBooksByCategory(categoryName);
            if (books.isEmpty()) {
                return new ResponseEntity<>(new Payload("Aucun livre trouvé pour la catégorie : " + categoryName, books), HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(new Payload("Livres récupérés pour la catégorie : " + categoryName, books), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new Payload("Erreur lors de la récupération des livres par catégorie : " + e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/book/{id}")
    public ResponseEntity<Payload> updateBook(@PathVariable int id, @RequestBody Map<String, Object> updates) {
        try {
            if (updates.containsKey("remise")) {
                double remise = Double.parseDouble(updates.get("remise").toString());
                Livre updatedBook = bookService.updateBook(id, remise);
                if (updatedBook != null) {
                    return new ResponseEntity<>(new Payload("Livre mis à jour avec succès.", updatedBook), HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(new Payload("Livre non trouvé pour l'ID : " + id), HttpStatus.NOT_FOUND);
                }
            } else {
                return new ResponseEntity<>(new Payload("Paramètre 'remise' manquant dans la requête"), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(new Payload("Erreur lors de la mise à jour du livre : " + e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/book")
    public ResponseEntity<Payload> addBook(@RequestBody Livre newBook) {
        try {
            // Validation des données du livre
            if (newBook.getTitre() == null || newBook.getAuteur() == null || newBook.getPrix() <= 0) {
                return new ResponseEntity<>(new Payload("Données du livre invalides."), HttpStatus.BAD_REQUEST);
            }

            // Appel du service pour ajouter le livre
            Livre savedBook = bookService.addBook(newBook);
            return new ResponseEntity<>(new Payload("Livre ajouté avec succès.", savedBook), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new Payload("Erreur lors de l'ajout du livre : " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
