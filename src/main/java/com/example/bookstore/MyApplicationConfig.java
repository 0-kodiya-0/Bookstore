/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.bookstore;

import com.example.bookstore.config.JacksonFeature;
import com.example.bookstore.exception.mapper.AuthorNotFoundExceptionMapper;
import com.example.bookstore.exception.mapper.BookNotFoundExceptionMapper;
import com.example.bookstore.exception.mapper.CartItemExistsExceptionMapper;
import com.example.bookstore.exception.mapper.CartItemNotFoundExceptionMapper;
import com.example.bookstore.exception.mapper.CartItemsNotFoundExceptionMapper;
import com.example.bookstore.exception.mapper.CustomerNotFoundExceptionMapper;
import com.example.bookstore.exception.mapper.GenericExceptionMapper;
import com.example.bookstore.exception.mapper.InvalidInputExceptionMapper;
import com.example.bookstore.exception.mapper.JsonProcessingExceptionMapper;
import com.example.bookstore.exception.mapper.OrderNotFoundExceptionMapper;
import com.example.bookstore.exception.mapper.OutOfStockExceptionMapper;
import com.example.bookstore.exception.mapper.UnrecognizedFieldExceptionMapper;
import com.example.bookstore.exception.mapper.WebApplicationExceptionMapper;
import com.example.bookstore.filters.RequestLoggingFilter;
import com.example.bookstore.filters.ResponseLoggingFilter;
import com.example.bookstore.filters.TrailingSlashFilter;
import com.example.bookstore.resources.AuthorBaseResource;
import com.example.bookstore.resources.AuthorIdResource;
import com.example.bookstore.resources.BookBaseResource;
import com.example.bookstore.resources.BookIdResource;
import com.example.bookstore.resources.CartResource;
import com.example.bookstore.resources.CustomerBaseResource;
import com.example.bookstore.resources.CustomerIdResource;
import com.example.bookstore.resources.CustomerOrderResource;
import org.glassfish.jersey.server.ResourceConfig;

/**
 *
 * @author HP
 */
public class MyApplicationConfig extends ResourceConfig{

    public MyApplicationConfig() {
        register(AuthorIdResource.class);
        register(AuthorBaseResource.class);
        register(BookBaseResource.class);
        register(BookIdResource.class);
        register(CustomerBaseResource.class);
        register(CustomerIdResource.class);
        register(CartResource.class);
        register(CustomerOrderResource.class);

        // Filters
        register(TrailingSlashFilter.class);
        register(RequestLoggingFilter.class);
        register(ResponseLoggingFilter.class);

        // Exception mappers
        register(BookNotFoundExceptionMapper.class);
        register(AuthorNotFoundExceptionMapper.class);
        register(CustomerNotFoundExceptionMapper.class);
        register(InvalidInputExceptionMapper.class);
        register(OutOfStockExceptionMapper.class);
        register(OrderNotFoundExceptionMapper.class);
        register(CartItemExistsExceptionMapper.class);
        register(CartItemNotFoundExceptionMapper.class);
        register(CartItemsNotFoundExceptionMapper.class);
        register(UnrecognizedFieldExceptionMapper.class);

        register(GenericExceptionMapper.class);
        register(JsonProcessingExceptionMapper.class);
        register(JsonProcessingExceptionMapper.class);
        register(WebApplicationExceptionMapper.class);
        
        register(JacksonFeature.class);
    }
    
}
