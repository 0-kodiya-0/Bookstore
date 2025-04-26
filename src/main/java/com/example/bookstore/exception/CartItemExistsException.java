/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.bookstore.exception;

/**
 *
 * @author HP
 */
public class CartItemExistsException extends RuntimeException {
    public CartItemExistsException(String message) {
        super(message);
    }
    
    public CartItemExistsException(Long bookId) {
        super("Book item with ID " + bookId + " exists.");
    }
}
