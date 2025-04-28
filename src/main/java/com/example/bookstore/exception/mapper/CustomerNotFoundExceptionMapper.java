package com.example.bookstore.exception.mapper;

import com.example.bookstore.exception.CustomerNotFoundException;
import com.example.bookstore.models.ErrorResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class CustomerNotFoundExceptionMapper implements ExceptionMapper<CustomerNotFoundException> {
    
    @Override
    public Response toResponse(CustomerNotFoundException exception) {
        ErrorResponse error = new ErrorResponse("Customer Not Found", exception.getMessage());
        return Response.status(Response.Status.NOT_FOUND)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}