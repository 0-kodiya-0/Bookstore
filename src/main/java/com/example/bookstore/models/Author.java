package com.example.bookstore.models;

import java.io.Serializable;

public class Author implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    private String biography;
    
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
}