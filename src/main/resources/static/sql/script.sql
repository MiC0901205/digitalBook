CREATE DATABASE IF NOT EXISTS readify_db;

USE readify_db;

-- Création de la table UTILISATEUR
CREATE TABLE IF NOT EXISTS UTILISATEUR (
    idUtilisateur INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(50),
    prenom VARCHAR(50)
);

-- Création de la table CARTE_DE_PAIEMENT
CREATE TABLE IF NOT EXISTS CARTE_DE_PAIEMENT (
    idCarte INT PRIMARY KEY AUTO_INCREMENT,
    numeroCarte VARCHAR(16),
    dateExpiration DATE,
    idUtilisateur INT,
    FOREIGN KEY (idUtilisateur) REFERENCES UTILISATEUR(idUtilisateur)
);

-- Création de la table ADRESSE
CREATE TABLE IF NOT EXISTS ADRESSE (
    idAdresse INT PRIMARY KEY AUTO_INCREMENT,
    rue VARCHAR(100),
    ville VARCHAR(50),
    codePostal VARCHAR(10),
    idUtilisateur INT,
    FOREIGN KEY (idUtilisateur) REFERENCES UTILISATEUR(idUtilisateur)
);

-- Création de la table CATEGORIE
CREATE TABLE IF NOT EXISTS CATEGORIE (
    idCategorie INT PRIMARY KEY AUTO_INCREMENT,
    nomCategorie VARCHAR(50)
);

-- Création de la table PARAMETRES
CREATE TABLE IF NOT EXISTS PARAMETRES (
    idParametre INT PRIMARY KEY AUTO_INCREMENT,
    cle VARCHAR(50),
    valeur VARCHAR(50),
    idUtilisateur INT,
    FOREIGN KEY (idUtilisateur) REFERENCES UTILISATEUR(idUtilisateur)
);

-- Création de la table COMMANDE
CREATE TABLE IF NOT EXISTS COMMANDE (
    idCommande INT PRIMARY KEY AUTO_INCREMENT,
    dateCommande DATE,
    idUtilisateur INT,
    FOREIGN KEY (idUtilisateur) REFERENCES UTILISATEUR(idUtilisateur)
);

-- Création de la table ARTICLE
CREATE TABLE IF NOT EXISTS ARTICLE (
    idArticle INT PRIMARY KEY AUTO_INCREMENT,
    nomArticle VARCHAR(50),
    prix DECIMAL(10, 2),
    idCategorie INT,
    FOREIGN KEY (idCategorie) REFERENCES CATEGORIE(idCategorie)
);

-- Création de la table PANIER
CREATE TABLE IF NOT EXISTS PANIER (
    idPanier INT PRIMARY KEY AUTO_INCREMENT,
    idUtilisateur INT,
    FOREIGN KEY (idUtilisateur) REFERENCES UTILISATEUR(idUtilisateur)
);

-- Création de la table COMMENTAIRE
CREATE TABLE IF NOT EXISTS COMMENTAIRE (
    idCommentaire INT PRIMARY KEY AUTO_INCREMENT,
    texte TEXT,
    idUtilisateur INT,
    idArticle INT,
    FOREIGN KEY (idUtilisateur) REFERENCES UTILISATEUR(idUtilisateur),
    FOREIGN KEY (idArticle) REFERENCES ARTICLE(idArticle)
);