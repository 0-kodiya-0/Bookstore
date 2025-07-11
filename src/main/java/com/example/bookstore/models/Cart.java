package com.example.bookstore.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Cart implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long customerId;
    private List<CartItem> items;
    
    public Cart() {
        this.items = new ArrayList<>();
    }
    
    public Cart(Long customerId) {
        this.customerId = customerId;
        this.items = new ArrayList<>();
    }
    
    public Cart(Long customerId, List<CartItem> items) {
        this.customerId = customerId;
        this.items = items != null ? items : new ArrayList<>();
    }
    
    // Getters and Setters
    public Long getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    
    public List<CartItem> getItems() {
        return items;
    }
    
    public CartItem getItems(Long bookId) {
        for (CartItem item : items) {
            if (item.getBookId().equals(bookId)) {
                return item;
            }
        }
        return null;
    }
    
    public void setItems(List<CartItem> items) {
        this.items = items;
    }
    
    // Helper methods
    public void addItem(CartItem item) {
        // Check if item already exists in cart
        for (CartItem existingItem : items) {
            if (existingItem.getBookId().equals(item.getBookId())) {
                existingItem.setQuantity(item.getQuantity());
                return;
            }
        }
        // If not, add new item
        items.add(item);
    }
    
    public void updateItem(Long bookId, int quantity) {
        for (CartItem item : items) {
            if (item.getBookId().equals(bookId)) {
                item.setQuantity(quantity);
                return;
            }
        }
    }
    
    public void removeItem(Long bookId) {
        items.removeIf(item -> item.getBookId().equals(bookId));
    }

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
    
    public int getTotalItemCount() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }
    
    @Override
    public String toString() {
        return "Cart{" + 
                "customerId=" + customerId + 
                ", items=" + (items != null ? items.toString() : "[]") + 
                '}';
    }
}
