package com.site.digitalBook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.site.digitalBook.entity.Livre;
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
    public ResponseEntity<List<Livre>> getAllBooks() {
        try {
            List<Livre> books = bookService.getAllBooks();
            if (books.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
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

    // Nouvelle méthode pour récupérer les livres par catégorie
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
}
