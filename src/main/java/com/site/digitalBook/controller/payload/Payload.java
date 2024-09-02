package com.site.digitalBook.controller.payload;

public class Payload {

    private String message;
    private Object data;
    private String token;
    private String role; // Ajouter un champ pour le rôle de l'utilisateur

    public Payload() {}

    public Payload(String message) {
        this.message = message;
    }

    public Payload(String message, Object data) {
        this.message = message;
        this.data = data;
    }

    public Payload(String message, Object data, String token) {
        this.message = message;
        this.data = data;
        this.token = token;
    }

    // Nouveau constructeur pour inclure le rôle
    public Payload(String message, Object data, String token, String role) {
        this.message = message;
        this.data = data;
        this.token = token;
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
