/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.bookstore.exception;

/**
 *
 * @author HP
 */
public class UnrecognizedFieldException extends RuntimeException {
    private String fieldName;
    
    public UnrecognizedFieldException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }
    
    public String getFieldName() {
        return fieldName;
    }
}
