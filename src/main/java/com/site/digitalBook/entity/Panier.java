package com.site.digitalBook.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Panier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = true)
    private User user;

    @ManyToMany
    @JoinTable(
        name = "CONTENIR", 
        joinColumns = @JoinColumn(name = "panier_id"),
        inverseJoinColumns = @JoinColumn(name = "livre_id")
    )
    private Set<Livre> livres = new HashSet<>();

    // Méthode utilitaire pour ajouter un livre au panier
    public void addBook(Livre book) {
        this.livres.add(book);
    }

    // Getters et setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<Livre> getLivres() {
        return livres;
    }

    public void setLivres(Set<Livre> livres) {
        this.livres = livres;
    }
}
