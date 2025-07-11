package com.example.bookstore.models;

import java.io.Serializable;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "cart_items")
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    // Many cart items belong to one customer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore // Prevent infinite recursion in JSON serialization
    private Customer customer;
    
    // Many cart items belong to one book
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    @JsonIgnore // Prevent infinite recursion
    private Book book;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    // Default constructor required by JPA
    public CartItem() {
    }
    
    public CartItem(Customer customer, Book book, Integer quantity) {
        this.customer = customer;
        this.book = book;
        this.quantity = quantity;
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
    
    public Book getBook() {
        return book;
    }
    
    public void setBook(Book book) {
        this.book = book;
    }
    
    // JSON serialization: return bookId instead of full book object
    @JsonProperty("bookId")
    public Long getBookId() {
        return book != null ? book.getId() : null;
    }
    
    // For backward compatibility with existing code
    public void setBookId(Long bookId) {
        // This will be handled in the repository layer when saving
        // We'll need to fetch the book entity first
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartItem)) return false;
        CartItem cartItem = (CartItem) o;
        return id != null && id.equals(cartItem.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CartItem{" + 
                "id=" + id + 
                ", customerId=" + (customer != null ? customer.getId() : null) +
                ", bookId=" + (book != null ? book.getId() : null) + 
                ", quantity=" + quantity + 
                '}';
    }
}