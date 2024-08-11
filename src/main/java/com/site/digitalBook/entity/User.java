package com.site.digitalBook.entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "utilisateur")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String nom;

    private String prenom;

    @Temporal(TemporalType.DATE)
    private Date dateNaissance;

    private boolean estActif;

    private String profil;

    private String email;

    private String mdp; 

    private String tel;
        
    private String questionSecrete; 

    private String reponseSecrete;

    private int panier;

    public User() {
    }

    // Constructeur avec email et mot de passe
    public User(String email, String mdp) {
        this.email = email;
        this.mdp = mdp;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public Date getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(Date dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public boolean isEstActif() {
        return estActif;
    }

    public void setEstActif(boolean estActif) {
        this.estActif = estActif;
    }

    public String getProfil() {
        return profil;
    }

    public void setProfil(String profil) {
        this.profil = profil;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMdp() {
        return mdp;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public int getPanier() {
        return panier;
    }

    public void setPanier(int panier) {
        this.panier = panier;
    }


	public String getQuestionSecrete() {
		return questionSecrete;
	}

	public void setQuestionSecrete(String questionSecrete) {
		this.questionSecrete = questionSecrete;
	}

	public String getReponseSecrete() {
		return reponseSecrete;
	}

	public void setReponseSecrete(String reponseSecrete) {
		this.reponseSecrete = reponseSecrete;
	}
	
    @Override
    public String toString() {
        return "User [id=" + id + ", nom=" + nom + ", prenom=" + prenom + ", dateNaissance=" + dateNaissance
                + ", estActif=" + estActif + ", profil=" + profil + ", email=" + email + ", mdp=" + mdp + ", tel=" + tel
                + ", panier=" + panier + "]";
    }

}
