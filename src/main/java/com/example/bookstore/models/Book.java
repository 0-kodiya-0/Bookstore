package com.example.bookstore.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "books")
public class Book implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "title", nullable = false, unique = true, length = 255)
    private String title;
    
    // Many books belong to one author
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnore // Prevent infinite recursion in JSON serialization
    private Author author;
    
    @Column(name = "isbn", length = 20)
    private String isbn;
    
    @Column(name = "publication_year", nullable = false)
    private Integer publicationYear;
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private Double price; 
    
    @Column(name = "stock", nullable = false)
    private Integer stock;
    
    // One book can be in many cart items
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    private List<CartItem> cartItems = new ArrayList<>();
    
    // One book can be in many order items
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<OrderItem> orderItems = new ArrayList<>();

    // Default constructor required by JPA
    public Book() {
    }

    public Book(Long id, String title, Author author, String isbn, Integer publicationYear, Double price, Integer stock) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publicationYear = publicationYear;
        this.price = price;
        this.stock = stock;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }
    
    // JSON serialization: return authorId instead of full author object
    @JsonProperty("authorId")
    public Long getAuthorId() {
        return author != null ? author.getId() : null;
    }
    
    // For backward compatibility with existing code
    public void setAuthorId(Long authorId) {
        // This will be handled in the repository layer when saving
        // We'll need to fetch the author entity first
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
    
    public List<CartItem> getCartItems() {
        return cartItems;
    }
    
    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }
    
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }
    
    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        Book book = (Book) o;
        return id != null && id.equals(book.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", authorId=" + (author != null ? author.getId() : null) +
                ", isbn='" + isbn + '\'' +
                ", publicationYear=" + publicationYear +
                ", price=" + price +
                ", stock=" + stock +
                '}';
    }
}