/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.bookstore.models;

/**
 *
 * @author HP
 */
public class ErrorResponse {
    private String error;
    private String message;
    
    public ErrorResponse() {
        // Default constructor for JAX-RS
    }
    
    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
    }
    
    // Getters and Setters
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}