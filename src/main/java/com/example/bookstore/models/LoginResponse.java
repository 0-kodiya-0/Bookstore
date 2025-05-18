package com.example.bookstore.models;

import java.io.Serializable;

public class LoginResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String token;
    private Customer customer;
    
    public LoginResponse() {
    }
    
    // Getters and Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @Override
    public String toString() {
        return "LoginResponse{" + "token=" + token + ", customer=" + customer.toString() + '}';
    }
}