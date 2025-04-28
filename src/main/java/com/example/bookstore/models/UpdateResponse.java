package com.example.bookstore.models;

public class UpdateResponse<T> {
    private T entity;
    private boolean updated;
    private int fieldsUpdated;
    
    public UpdateResponse() {
        
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
