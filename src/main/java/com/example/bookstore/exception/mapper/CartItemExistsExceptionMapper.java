package com.example.bookstore.exception.mapper;

import com.example.bookstore.exception.CartItemExistsException;
import com.example.bookstore.models.ErrorResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class CartItemExistsExceptionMapper implements ExceptionMapper<CartItemExistsException> {
    
    @Override
    public Response toResponse(CartItemExistsException exception) {
        ErrorResponse error = new ErrorResponse("Cart Item Exists", exception.getMessage());
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
