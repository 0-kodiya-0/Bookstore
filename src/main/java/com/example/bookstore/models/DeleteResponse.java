package com.example.bookstore.models;

public class DeleteResponse {
    private boolean deleted;
    private int count;
    
    public DeleteResponse() {
        
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
