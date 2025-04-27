package com.example.bookstore.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UnrecognizedFieldException extends RuntimeException {
    
    private static final Logger LOGGER = Logger.getLogger(UnrecognizedFieldException.class.getName());
    private final String fieldName;
    
    public UnrecognizedFieldException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
        LOGGER.log(Level.WARNING, "UnrecognizedFieldException: {0} [Field: {1}]", new Object[]{message, fieldName});
    }
    
    public String getFieldName() {
        return fieldName;
    }
}
