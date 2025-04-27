package com.example.bookstore.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

public class OrderNotFoundException extends RuntimeException {
    
    private static final Logger LOGGER = Logger.getLogger(OrderNotFoundException.class.getName());
    
    public OrderNotFoundException(String message) {
        super(message);
        LOGGER.log(Level.WARNING, "OrderNotFoundException: {0}", message);
    }
    
    public OrderNotFoundException(Long id) {
        super("Order with ID " + id + " does not exist.");
        LOGGER.log(Level.WARNING, "OrderNotFoundException: Order with ID {0} does not exist.", id);
    }
    
    public OrderNotFoundException(Long customerId, Long orderId) {
        super("Order with ID " + orderId + " not found for customer with ID " + customerId + ".");
        LOGGER.log(Level.WARNING, "OrderNotFoundException: Order with ID {0} not found for customer with ID {1}.", new Object[]{orderId, customerId});
    }
}