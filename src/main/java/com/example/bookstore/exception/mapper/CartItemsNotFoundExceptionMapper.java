package com.example.bookstore.exception.mapper;

import com.example.bookstore.exception.CartItemsNotFoundException;
import com.example.bookstore.models.ErrorResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class CartItemsNotFoundExceptionMapper implements ExceptionMapper<CartItemsNotFoundException> {
    
    @Override
    public Response toResponse(CartItemsNotFoundException exception) {
        ErrorResponse error = new ErrorResponse("Cart Items Not Found", exception.getMessage());
        return Response.status(Response.Status.NOT_FOUND)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
