package com.example.bookstore.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomerNotFoundException extends RuntimeException {
    
    private static final Logger LOGGER = Logger.getLogger(CustomerNotFoundException.class.getName());
    
    public CustomerNotFoundException(String message) {
        super(message);
        LOGGER.log(Level.WARNING, "CustomerNotFoundException: {0}", message);
    }
    
    public CustomerNotFoundException(Long id) {
        super("Customer with ID " + id + " does not exist.");
        LOGGER.log(Level.WARNING, "CustomerNotFoundException: Customer with ID {0} does not exist.", id);
    }
}
