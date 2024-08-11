package com.site.digitalBook.controller.payload;

public class Payload {

    private String message;
    private Object data;
    private String token;

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
}
