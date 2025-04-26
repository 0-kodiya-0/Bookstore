/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.bookstore.exception.mapper;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.example.bookstore.models.ErrorResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author HP
 */
@Provider
public class UnrecognizedFieldExceptionMapper implements ExceptionMapper<UnrecognizedPropertyException> {

    @Override
    public Response toResponse(UnrecognizedPropertyException exception) {
        // Safely extract the field name
        String fieldName = exception.getPropertyName();
        
        // Get class name safely, checking for null
        String className = "Unknown";
        if (exception.getTargetType() != null) {
            className = exception.getTargetType().getSimpleName();
        }
        
        // Parse the error message to get valid field names when available
        String validFieldsInfo = "";
        String exceptionMessage = exception.getMessage();
        if (exceptionMessage != null && exceptionMessage.contains("known properties:")) {
            int startIdx = exceptionMessage.indexOf("known properties:") + 16;
            int endIdx = exceptionMessage.indexOf("]", startIdx) + 1;
            if (startIdx > 16 && endIdx > 0 && endIdx > startIdx) {
                String knownPropsStr = exceptionMessage.substring(startIdx, endIdx);
                validFieldsInfo = ". Valid fields are " + knownPropsStr;
            }
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            "Invalid Field", 
            "The field '" + fieldName + "' is not recognized for " + className + 
            validFieldsInfo + ". Please check your request payload."
        );
        
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}