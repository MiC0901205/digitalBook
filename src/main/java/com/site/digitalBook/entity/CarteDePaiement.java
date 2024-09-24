package com.site.digitalBook.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.time.LocalDate;

@Entity
@Table(name = "carte_de_paiement")
public class CarteDePaiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")  // Champ en français dans la BDD
    private int id; 

    @Column(name = "numero_carte")  // Champ en français pour le numéro de carte
    private byte[] cardNumber;

    @Column(name = "cvv")  // Champ CVV en français (ça reste "cvv" car c'est déjà en français)
    private byte[] cvv;

    @Column(name = "date_expiration")  // Champ pour la date d'expiration
    private String expiryDate;

    @Column(name = "utilisateur_id")  // Champ pour l'ID de l'utilisateur
    private int userId;

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(byte[] cardNumber) {
        this.cardNumber = cardNumber;
    }

    public byte[] getCvv() {
        return cvv;
    }

    public void setCvv(byte[] cvv) {
        this.cvv = cvv;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
