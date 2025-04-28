package com.example.bookstore.exception.mapper;

import com.example.bookstore.exception.CartItemNotFoundException;
import com.example.bookstore.models.ErrorResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class CartItemNotFoundExceptionMapper implements ExceptionMapper<CartItemNotFoundException> {

    @Override
    public Response toResponse(CartItemNotFoundException exception) {
        ErrorResponse error = new ErrorResponse("Cart Item Not Found", exception.getMessage());
        return Response.status(Response.Status.NOT_FOUND)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
