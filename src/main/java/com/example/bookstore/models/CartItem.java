package com.example.bookstore.models;

import java.io.Serializable;

public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long bookId;
    private Integer quantity;
    
    public CartItem() {
        
    }
    
    public CartItem(Long bookId, Integer quantity) {
        this.bookId = bookId;
        this.quantity = quantity;
    }
    
    // Getters and Setters
    public Long getBookId() {
        return bookId;
    }
    
    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "CartItem{" + "bookId=" + bookId + ", quantity=" + quantity + '}';
    }
}