/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.bookstore.models;

/**
 *
 * @author HP
 */
public class DeleteResponse {
    private boolean deleted;
    private int count;
    
    public DeleteResponse() {
        // Default constructor for JAX-RS
    }
    
    public DeleteResponse(boolean deleted, int count) {
        this.deleted = deleted;
        this.count = count;
    }
    
    // Getters and Setters
    public boolean isDeleted() {
        return deleted;
    }
    
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
}
