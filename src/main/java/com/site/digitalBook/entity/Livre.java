package com.site.digitalBook.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	private String titre;
    private String auteur;
    private double prix;
    private int stock;
    private double remise;
    private boolean estVendable;
    private String photos;  
    private LocalDate datePublication;
    private String ISBN;
    private String editeur;
    
    @ManyToMany
    @JoinTable(
            name = "INCLURE",  // Nom de la table associative
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
        )
    private Set<Categorie> categories = new HashSet<>();

    public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTitre() {
		return titre;
	}
	public void setTitre(String titre) {
		this.titre = titre;
	}
	public String getAuteur() {
		return auteur;
	}
	public void setAuteur(String auteur) {
		this.auteur = auteur;
	}
	public double getPrix() {
		return prix;
	}
	public void setPrix(double prix) {
		this.prix = prix;
	}
	public int getStock() {
		return stock;
	}
	public void setStock(int stock) {
		this.stock = stock;
	}
	public double getRemise() {
		return remise;
	}
	public void setRemise(double remise) {
		this.remise = remise;
	}
	public boolean isEstVendable() {
		return estVendable;
	}
	public void setEstVendable(boolean estVendable) {
		this.estVendable = estVendable;
	}
	public String getPhotos() {
		return photos;
	}
	public void setPhotos(String photos) {
		this.photos = photos;
	}
	public LocalDate getDatePublication() {
		return datePublication;
	}
	public void setDatePublication(LocalDate datePublication) {
		this.datePublication = datePublication;
	}
	public String getISBN() {
		return ISBN;
	}
	public void setISBN(String iSBN) {
		ISBN = iSBN;
	}
	public String getEditeur() {
		return editeur;
	}
	public void setEditeur(String editeur) {
		this.editeur = editeur;
	}  
}
