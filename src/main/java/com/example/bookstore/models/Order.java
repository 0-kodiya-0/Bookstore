package com.example.bookstore.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long customerId;
    
    // Change LocalDateTime to java.util.Date which is better supported by Gson
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date orderDate;
    private List<OrderItem> items;
    private Double totalAmount;
    
    public Order() {
        this.items = new ArrayList<>();
    }
    
    public Order(Long id, Long customerId) {
        this.id = id;
        this.customerId = customerId;
        
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.orderDate = calendar.getTime();
        
        this.items = new ArrayList<>();
        this.totalAmount = 0.0;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    
    public Date getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }
    
    public List<OrderItem> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
    
    public Double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    // Helper method to calculate total
    public void calculateTotal() {
        this.totalAmount = items.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();
    }
}