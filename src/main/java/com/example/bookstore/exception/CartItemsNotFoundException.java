/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.bookstore.exception;

/**
 *
 * @author HP
 */
public class CartItemsNotFoundException extends RuntimeException {
    
    public CartItemsNotFoundException(String message) {
        super(message);
    }
    
    public CartItemsNotFoundException(Long customerId) {
        super("No items found in cart for customer with ID " + customerId);
    }
}
