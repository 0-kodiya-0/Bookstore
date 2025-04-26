/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.bookstore.models;

/**
 *
 * @author HP
 */
public class UpdateResponse<T> {
    private T entity;
    private boolean updated;
    private int fieldsUpdated;
    
    public UpdateResponse() {
        // Default constructor for JAX-RS
    }
    
    public UpdateResponse(T entity, boolean updated, int fieldsUpdated) {
        this.entity = entity;
        this.updated = updated;
        this.fieldsUpdated = fieldsUpdated;
    }
    
    // Getters and Setters
    public T getEntity() {
        return entity;
    }
    
    public void setEntity(T entity) {
        this.entity = entity;
    }
    
    public boolean isUpdated() {
        return updated;
    }
    
    public void setUpdated(boolean updated) {
        this.updated = updated;
    }
    
    public int getFieldsUpdated() {
        return fieldsUpdated;
    }
    
    public void setFieldsUpdated(int fieldsUpdated) {
        this.fieldsUpdated = fieldsUpdated;
    }
}
