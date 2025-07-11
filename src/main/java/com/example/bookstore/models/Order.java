package com.example.bookstore.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.persistence.*;

@Entity
@Table(name = "orders")
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    // Many orders belong to one customer
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore // Prevent infinite recursion in JSON serialization
    private Customer customer;
    
    @Column(name = "order_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date orderDate;
    
    // One order can have many order items
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private Double totalAmount;
    
    // Default constructor required by JPA
    public Order() {
        this.items = new ArrayList<>();
    }
    
    public Order(Long id, Customer customer) {
        this.id = id;
        this.customer = customer;
        
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
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    // JSON serialization: return customerId instead of full customer object
    @JsonProperty("customerId")
    public Long getCustomerId() {
        return customer != null ? customer.getId() : null;
    }
    
    // For backward compatibility with existing code
    public void setCustomerId(Long customerId) {
        // This will be handled in the repository layer when saving
        // We'll need to fetch the customer entity first
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
    
    // Helper method to add an order item
    public void addOrderItem(OrderItem orderItem) {
        items.add(orderItem);
        orderItem.setOrder(this);
    }
    
    // Helper method to remove an order item
    public void removeOrderItem(OrderItem orderItem) {
        items.remove(orderItem);
        orderItem.setOrder(null);
    }
    
    // Helper method to calculate total
    public void calculateTotal() {
        this.totalAmount = items.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return id != null && id.equals(order.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customerId=" + (customer != null ? customer.getId() : null) +
                ", orderDate=" + orderDate +
                ", totalAmount=" + totalAmount +
                ", itemsCount=" + (items != null ? items.size() : 0) +
                '}';
    }
}