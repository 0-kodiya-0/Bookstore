/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.bookstore.exception;

/**
 *
 * @author HP
 */
public class CartItemNotFoundException extends RuntimeException {

    public CartItemNotFoundException(String message) {
        super(message);
    }

    public CartItemNotFoundException(Long customerId, Long bookId) {
        super("Book with ID " + bookId + " not found in cart with customer ID " + customerId);
    }
}
