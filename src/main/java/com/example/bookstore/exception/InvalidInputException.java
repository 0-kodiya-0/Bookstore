package com.example.bookstore.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

public class InvalidInputException extends RuntimeException {
    
    private static final Logger LOGGER = Logger.getLogger(InvalidInputException.class.getName());
    
    public InvalidInputException(String message) {
        super(message);
        LOGGER.log(Level.WARNING, "InvalidInputException: " + message);
    }
}

