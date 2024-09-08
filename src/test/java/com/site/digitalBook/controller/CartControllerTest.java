package com.site.digitalBook.controller;

import com.site.digitalBook.entity.Livre;
import com.site.digitalBook.entity.Panier;
import com.site.digitalBook.service.CartService;
import com.site.digitalBook.controller.payload.Payload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class CartControllerTest {

    @InjectMocks
    private CartController cartController;

    @Mock
    private CartService cartService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateCart_Success() {
        Panier cart = new Panier();
        when(cartService.createCart(anyInt())).thenReturn(cart);

        ResponseEntity<Payload> response = cartController.createCart(1);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Panier créé avec succès.", response.getBody().getMessage());
    }

    @Test
    public void testCreateCart_Error() {
        when(cartService.createCart(anyInt())).thenThrow(new RuntimeException("Erreur"));

        ResponseEntity<Payload> response = cartController.createCart(1);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Erreur lors de la création du panier : Erreur", response.getBody().getMessage());
    }

    @Test
    public void testGetCartItemCount_Success() {
        when(cartService.getCartItemCount(anyInt())).thenReturn(5);

        ResponseEntity<Payload> response = cartController.getCartItemCount(1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Nombre d'articles récupéré avec succès.", response.getBody().getMessage());
        assertEquals(5, response.getBody().getData());
    }

    @Test
    public void testGetCartItemCount_Error() {
        when(cartService.getCartItemCount(anyInt())).thenThrow(new RuntimeException("Erreur"));

        ResponseEntity<Payload> response = cartController.getCartItemCount(1);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Erreur lors de la récupération du nombre d'articles : Erreur", response.getBody().getMessage());
    }

    @Test
    public void testGetCartItems_Success() {
        Panier cart = new Panier();
        cart.setLivres(Collections.singleton(new Livre()));
        when(cartService.getCartByUserId(anyInt())).thenReturn(cart);

        ResponseEntity<Payload> response = cartController.getCartItems(1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Éléments du panier récupérés avec succès.", response.getBody().getMessage());
        assertEquals(cart.getLivres(), response.getBody().getData());
    }

    @Test
    public void testGetCartItems_NotFound() {
        when(cartService.getCartByUserId(anyInt())).thenReturn(null);

        ResponseEntity<Payload> response = cartController.getCartItems(1);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Panier non trouvé pour l'utilisateur ID : 1", response.getBody().getMessage());
    }

    @Test
    public void testAddItem_Success() {
        Panier cart = new Panier();
        try {
			when(cartService.addItemToCart(anyInt(), anyInt())).thenReturn(cart);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        ResponseEntity<Payload> response = cartController.addItem(1, 1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Article ajouté au panier avec succès.", response.getBody().getMessage());
    }

    @Test
    public void testAddItem_Error() {
        try {
			when(cartService.addItemToCart(anyInt(), anyInt())).thenThrow(new RuntimeException("Erreur"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        ResponseEntity<Payload> response = cartController.addItem(1, 1);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Erreur lors de l'ajout de l'article au panier : Erreur", response.getBody().getMessage());
    }

    @Test
    public void testRemoveItem_Success() {
        Panier cart = new Panier();
        when(cartService.removeItemFromCart(anyInt(), anyInt())).thenReturn(cart);

        ResponseEntity<Payload> response = cartController.removeItem(1, 1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Article retiré du panier avec succès.", response.getBody().getMessage());
    }

    @Test
    public void testRemoveItem_Error() {
        when(cartService.removeItemFromCart(anyInt(), anyInt())).thenThrow(new RuntimeException("Erreur"));

        ResponseEntity<Payload> response = cartController.removeItem(1, 1);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Erreur lors de la suppression de l'article du panier : Erreur", response.getBody().getMessage());
    }

    @Test
    public void testClearCart_Success() {
        doNothing().when(cartService).clearCart(anyInt());

        ResponseEntity<Payload> response = cartController.clearCart(1);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals("Panier vidé avec succès.", response.getBody().getMessage());
    }

    @Test
    public void testClearCart_Error() {
        doThrow(new RuntimeException("Erreur")).when(cartService).clearCart(anyInt());

        ResponseEntity<Payload> response = cartController.clearCart(1);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Erreur lors du vidage du panier : Erreur", response.getBody().getMessage());
    }
}
