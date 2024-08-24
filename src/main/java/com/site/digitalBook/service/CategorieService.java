package com.site.digitalBook.service;

import com.site.digitalBook.entity.Categorie;
import com.site.digitalBook.repository.CategorieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategorieService {

    private final CategorieRepository categoryRepository;

    @Autowired
    public CategorieService(CategorieRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Categorie> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Categorie saveCategory(Categorie category) {
        return categoryRepository.save(category);
    }

}
