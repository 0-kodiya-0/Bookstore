package com.example.bookstore.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

public class OutOfStockException extends RuntimeException {
    
    private static final Logger LOGGER = Logger.getLogger(OutOfStockException.class.getName());
    
    public OutOfStockException(String message) {
        super(message);
        LOGGER.log(Level.WARNING, "OutOfStockException: {0}", message);
    }
    
    public OutOfStockException(Long bookId, int requested, int available) {
        super("Book with ID " + bookId + " has insufficient stock. Requested: " + 
              requested + ", Available: " + available + ".");
        LOGGER.log(Level.WARNING, "OutOfStockException: Book with ID {0} has insufficient stock. Requested: {1}, Available: {2}.", new Object[]{bookId, requested, available});
    }
}