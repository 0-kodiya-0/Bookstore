/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.bookstore.exception.mapper;

import com.example.bookstore.exception.InvalidInputException;
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
public class InvalidInputExceptionMapper implements ExceptionMapper<InvalidInputException> {
    
    @Override
    public Response toResponse(InvalidInputException exception) {
        ErrorResponse error = new ErrorResponse("Invalid Input", exception.getMessage());
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
