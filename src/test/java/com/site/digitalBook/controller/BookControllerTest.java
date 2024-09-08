package com.site.digitalBook.controller;

import com.site.digitalBook.entity.Livre;
import com.site.digitalBook.service.BookService;
import com.site.digitalBook.controller.payload.Payload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class BookControllerTest {

    @InjectMocks
    private BookController bookController;

    @Mock
    private BookService bookService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllBooks_Success() {
        Livre book = new Livre();
        when(bookService.getAllBooks()).thenReturn(Collections.singletonList(book));

        ResponseEntity<Payload> response = bookController.getAllBooks();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Livres récupérés avec succès.", response.getBody().getMessage());
    }

    @Test
    public void testGetAllBooks_NoContent() {
        when(bookService.getAllBooks()).thenReturn(Collections.emptyList());

        ResponseEntity<Payload> response = bookController.getAllBooks();
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals("Aucun livre trouvé.", response.getBody().getMessage());
    }

    @Test
    public void testGetBookById_Success() {
        Livre book = new Livre();
        when(bookService.getBookById(anyInt())).thenReturn(book);

        ResponseEntity<Payload> response = bookController.getBookById(1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Livre récupéré avec succès.", response.getBody().getMessage());
    }

    @Test
    public void testGetBookById_NotFound() {
        when(bookService.getBookById(anyInt())).thenReturn(null);

        ResponseEntity<Payload> response = bookController.getBookById(1);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Livre non trouvé pour l'ID : 1", response.getBody().getMessage());
    }

    @Test
    public void testGetBooksByCategory_Success() {
        Livre book = new Livre();
        when(bookService.getBooksByCategory(anyString())).thenReturn(Collections.singletonList(book));

        ResponseEntity<Payload> response = bookController.getBooksByCategory("Fiction");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Livres récupérés pour la catégorie : Fiction", response.getBody().getMessage());
    }

    @Test
    public void testGetBooksByCategory_NoContent() {
        when(bookService.getBooksByCategory(anyString())).thenReturn(Collections.emptyList());

        ResponseEntity<Payload> response = bookController.getBooksByCategory("Fiction");
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals("Aucun livre trouvé pour la catégorie : Fiction", response.getBody().getMessage());
    }

    @Test
    public void testUpdateBook_Success() {
        Livre updatedBook = new Livre();
        when(bookService.updateBook(anyInt(), anyDouble())).thenReturn(updatedBook);

        ResponseEntity<Payload> response = bookController.updateBook(1, Map.of("remise", 10.0));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Livre mis à jour avec succès.", response.getBody().getMessage());
    }

    @Test
    public void testUpdateBook_BadRequest() {
        ResponseEntity<Payload> response = bookController.updateBook(1, Map.of());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Paramètre 'remise' manquant dans la requête", response.getBody().getMessage());
    }

    @Test
    public void testAddBook_Success() {
        Livre newBook = new Livre();
        when(bookService.addBook(any(Livre.class))).thenReturn(newBook);

        ResponseEntity<Payload> response = bookController.addBook(newBook);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Livre ajouté avec succès.", response.getBody().getMessage());
    }

    @Test
    public void testAddBook_BadRequest() {
        Livre newBook = new Livre();
        newBook.setTitre(null); // Invalid data
        ResponseEntity<Payload> response = bookController.addBook(newBook);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Données du livre invalides.", response.getBody().getMessage());
    }
}
