package com.example.bookstore.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CartItemExistsException extends RuntimeException {
    
    private static final Logger LOGGER = Logger.getLogger(CartItemExistsException.class.getName());
    
    public CartItemExistsException(String message) {
        super(message);
        LOGGER.log(Level.WARNING, "CartItemExistsException: {0}", message);
    }
    
    public CartItemExistsException(Long bookId) {
        super("Book item with ID " + bookId + " exists.");
        LOGGER.log(Level.WARNING, "CartItemExistsException: Book item with ID {0} exists.", bookId);
    }
}
