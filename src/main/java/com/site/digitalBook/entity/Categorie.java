package com.site.digitalBook.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Column;

@Entity
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double remise;
    private boolean remiseCumulable;

    @Column(name = "parent_id")
    private Long parentId; // ID de la catégorie parente, NULL si catégorie principale

    @Column(name = "type")
    private String type; // Exemple : 'Fiction', 'Non-Fiction'

    @Column(name = "description")
    private String description; // Description de la catégorie

    @ManyToMany(mappedBy = "categories")
    private Set<Book> books = new HashSet<>();

    // Constructeurs
    public Categorie() {}

    public Categorie(String name, double remise, boolean remiseCumulable, Long parentId, String type, String description) {
        this.name = name;
        this.remise = remise;
        this.remiseCumulable = remiseCumulable;
        this.parentId = parentId;
        this.type = type;
        this.description = description;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getRemise() { return remise; }
    public void setRemise(double remise) { this.remise = remise; }
    public boolean isRemiseCumulable() { return remiseCumulable; }
    public void setRemiseCumulable(boolean remiseCumulable) { this.remiseCumulable = remiseCumulable; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Set<Book> getBooks() { return books; }
    public void setBooks(Set<Book> books) { this.books = books; }
}
