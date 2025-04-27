package com.example.bookstore.exception.mapper;

import com.example.bookstore.models.ErrorResponse;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {
    
    @Override
    public Response toResponse(JsonProcessingException exception) {
        String message = exception.getMessage();
        
        // If it's a JsonParseException, extract the specific error
        if (exception instanceof JsonParseException) {
            JsonParseException parseException = (JsonParseException) exception;
            message = parseException.getOriginalMessage();
            
            // If we can extract line and column information, use it
            if (parseException.getLocation() != null) {
                message += String.format(" at line: %d, column: %d",
                        parseException.getLocation().getLineNr(),
                        parseException.getLocation().getColumnNr());
            }
        }
        
        // Clean up the message - remove full stack trace if present
        if (message != null && message.contains("\n at [Source:")) {
            message = message.substring(0, message.indexOf("\n"));
        }
        
        ErrorResponse error = new ErrorResponse("JSON Processing Error", 
                                             "Invalid JSON format: " + message);
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}