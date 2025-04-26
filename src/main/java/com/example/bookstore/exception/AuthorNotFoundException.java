/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.bookstore.exception;

/**
 *
 * @author HP
 */
public class AuthorNotFoundException extends RuntimeException {
    
    public AuthorNotFoundException(String message) {
        super(message);
    }
    
    public AuthorNotFoundException(Long id) {
        super("Author with ID " + id + " does not exist.");
    }
}
