package com.example.bookstore.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthorNotFoundException extends RuntimeException {

    private static final Logger LOGGER = Logger.getLogger(AuthorNotFoundException.class.getName());

    public AuthorNotFoundException(String message) {
        super(message);
        LOGGER.log(Level.WARNING, "AuthorNotFoundException: {0}", message);
    }

    public AuthorNotFoundException(Long id) {
        super("Author with ID " + id + " does not exist.");
        LOGGER.log(Level.WARNING, "AuthorNotFoundException: Author with ID {0} does not exist.", id);
    }
}
