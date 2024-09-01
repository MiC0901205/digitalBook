package com.site.digitalBook.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;

@Entity
@Table(name = "commandes")
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "prix_total", nullable = false)
    private BigDecimal prixTotal;

    @Column(name = "methode_paiement", nullable = false)
    private String methodePaiement;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @ElementCollection
    @CollectionTable(name = "ATTACHER", joinColumns = @JoinColumn(name = "commande_id"))
    @Column(name = "livre_id")
    private Set<Integer> livreIds = new HashSet<>();


    // Getters et Setters
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

    public BigDecimal getPrixTotal() {
        return prixTotal;
    }

    public void setPrixTotal(BigDecimal prixTotal) {
        this.prixTotal = prixTotal;
    }

    public String getMethodePaiement() {
        return methodePaiement;
    }

    public void setMethodePaiement(String methodePaiement) {
        this.methodePaiement = methodePaiement;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Set<Integer> getLivreIds() {
        return livreIds;
    }

    public void setLivreIds(Set<Integer> livreIds) {
        this.livreIds = livreIds;
    }
}