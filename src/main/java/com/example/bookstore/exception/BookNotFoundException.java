package com.example.bookstore.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BookNotFoundException extends RuntimeException {
    
    private static final Logger LOGGER = Logger.getLogger(BookNotFoundException.class.getName());
    
    public BookNotFoundException(String message) {
        super(message);
        LOGGER.log(Level.WARNING, "BookNotFoundException: {0}", message);
    }
    
    public BookNotFoundException(Long id) {
        super("Book with ID " + id + " does not exist.");
        LOGGER.log(Level.WARNING, "BookNotFoundException: Book with ID {0} does not exist.", id);
    }
}
