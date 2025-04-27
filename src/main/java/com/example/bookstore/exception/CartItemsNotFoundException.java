package com.example.bookstore.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CartItemsNotFoundException extends RuntimeException {
    
    private static final Logger LOGGER = Logger.getLogger(CartItemsNotFoundException.class.getName());
    
    public CartItemsNotFoundException(String message) {
        super(message);
        LOGGER.log(Level.WARNING, "CartItemsNotFoundException: {0}", message);
    }
    
    public CartItemsNotFoundException(Long customerId) {
        super("No items found in cart for customer with ID " + customerId);
        LOGGER.log(Level.WARNING, "CartItemsNotFoundException: No items found in cart for customer with ID {0}", customerId);
    }
}
