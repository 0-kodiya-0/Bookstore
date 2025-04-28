package com.example.bookstore.exception.mapper;

import com.example.bookstore.exception.OutOfStockException;
import com.example.bookstore.models.ErrorResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class OutOfStockExceptionMapper implements ExceptionMapper<OutOfStockException> {
    
    @Override
    public Response toResponse(OutOfStockException exception) {
        ErrorResponse error = new ErrorResponse("Out of Stock", exception.getMessage());
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
