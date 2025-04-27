package com.example.bookstore.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CartItemNotFoundException extends RuntimeException {

    private static final Logger LOGGER = Logger.getLogger(CartItemNotFoundException.class.getName());

    public CartItemNotFoundException(String message) {
        super(message);
        LOGGER.log(Level.WARNING, "CartItemNotFoundException: {0}", message);
    }

    public CartItemNotFoundException(Long customerId, Long bookId) {
        super("Book with ID " + bookId + " not found in cart with customer ID " + customerId);
        LOGGER.log(Level.WARNING, "CartItemNotFoundException: Book with ID {0} not found in cart with customer ID {1}", new Object[]{bookId, customerId});
    }
}
