package com.example.bookstore.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "authors")
public class Author implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "name", nullable = false, unique = true, length = 255)
    private String name;
    
    @Column(name = "biography", columnDefinition = "TEXT")
    private String biography;
    
    // One author can have many books
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore // Prevent infinite recursion in JSON serialization
    private List<Book> books = new ArrayList<>();
    
    // Default constructor required by JPA
    public Author() {
    }
    
    public Author(Long id, String name, String biography) {
        this.id = id;
        this.name = name;
        this.biography = biography;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getBiography() {
        return biography;
    }
    
    public void setBiography(String biography) {
        this.biography = biography;
    }
    
    public List<Book> getBooks() {
        return books;
    }
    
    public void setBooks(List<Book> books) {
        this.books = books;
    }
    
    // Helper method to add a book
    public void addBook(Book book) {
        books.add(book);
        book.setAuthor(this);
    }
    
    // Helper method to remove a book
    public void removeBook(Book book) {
        books.remove(book);
        book.setAuthor(null);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Author)) return false;
        Author author = (Author) o;
        return id != null && id.equals(author.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Author{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", biography='" + biography + '\'' +
                '}';
    }
}