package com.example.bookstore.exception.mapper;

import com.example.bookstore.models.ErrorResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
    
    @Override
    public Response toResponse(WebApplicationException exception) {
        Response originalResponse = exception.getResponse();
        
        ErrorResponse error = new ErrorResponse(
            originalResponse.getStatusInfo().getReasonPhrase(),
            exception.getMessage() != null ? exception.getMessage() : "An error occurred"
        );
        
        return Response.status(originalResponse.getStatus())
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}