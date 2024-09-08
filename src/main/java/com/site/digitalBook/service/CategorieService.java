package com.site.digitalBook.service;

import com.site.digitalBook.entity.Categorie;
import com.site.digitalBook.repository.CategorieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategorieService {

    private final CategorieRepository categoryRepository;
    private static final Logger logger = LoggerFactory.getLogger(CategorieService.class);

    @Autowired
    public CategorieService(CategorieRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Categorie> getAllCategories() {
        logger.info("Récupération de toutes les catégories.");
        List<Categorie> categories = categoryRepository.findAll();
        logger.info("Nombre de catégories récupérées: {}", categories.size());
        return categories;
    }

    public Categorie saveCategory(Categorie category) {
        logger.info("Sauvegarde de la catégorie: {}", category);
        Categorie savedCategory = categoryRepository.save(category);
        logger.info("Catégorie sauvegardée avec l'ID: {}", savedCategory.getId());
        return savedCategory;
    }

}
