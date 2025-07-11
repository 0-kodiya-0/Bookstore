package com.example.bookstore.models;

import java.io.Serializable;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "order_items")
public class OrderItem implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    // Many order items belong to one order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore // Prevent infinite recursion in JSON serialization
    private Order order;
    
    // Many order items belong to one book
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    @JsonIgnore // Prevent infinite recursion
    private Book book;
    
    @Column(name = "book_title", nullable = false, length = 255)
    private String bookTitle;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private Double price;

    // Default constructor required by JPA
    public OrderItem() {
    }

    public OrderItem(Book book, String bookTitle, Integer quantity, Double price) {
        this.book = book;
        this.bookTitle = bookTitle;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Order getOrder() {
        return order;
    }
    
    public void setOrder(Order order) {
        this.order = order;
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

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem)) return false;
        OrderItem orderItem = (OrderItem) o;
        return id != null && id.equals(orderItem.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", bookId=" + (book != null ? book.getId() : null) +
                ", bookTitle='" + bookTitle + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}