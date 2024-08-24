package com.site.digitalBook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.site.digitalBook.controller.payload.Payload;
import com.site.digitalBook.entity.Book;
import com.site.digitalBook.exception.BookNotFoundException;
import com.site.digitalBook.service.BookService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    
    @GetMapping("/books")
    public ResponseEntity<List<Book>> getAllBooks() {
        try {
            List<Book> books = bookService.getAllBooks();
            if (books.isEmpty()) {
                // Retourne une réponse vide mais avec un code 200
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            // En cas d'erreur, renvoie un statut BAD_REQUEST et un message d'erreur
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}

