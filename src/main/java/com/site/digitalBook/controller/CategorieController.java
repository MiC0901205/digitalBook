package com.site.digitalBook.controller;

import com.site.digitalBook.controller.payload.Payload;
import com.site.digitalBook.entity.Categorie;
import com.site.digitalBook.exception.CategorieNotFoundException;
import com.site.digitalBook.service.CategorieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CategorieController {

    private final CategorieService categorieService;

    @Autowired
    public CategorieController(CategorieService categorieService) {
        this.categorieService = categorieService;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Categorie>> getAllCategories() {
        try {
            List<Categorie> categories = categorieService.getAllCategories();
            if (categories.isEmpty()) {
                // Retourne une réponse avec le statut HTTP 204 No Content
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(categories, HttpStatus.OK);
        } catch (Exception e) {
            // En cas d'erreur, renvoie une réponse avec un statut HTTP 400 Bad Request
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
