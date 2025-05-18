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
import com.example.bookstore.filters.JwtAuthenticationFilter;
import com.example.bookstore.filters.RequestLoggingFilter;
import com.example.bookstore.filters.ResponseLoggingFilter;
import com.example.bookstore.filters.TrailingSlashFilter;
import com.example.bookstore.resources.AuthResource;
import com.example.bookstore.resources.AuthorBaseResource;
import com.example.bookstore.resources.AuthorIdResource;
import com.example.bookstore.resources.BookBaseResource;
import com.example.bookstore.resources.BookIdResource;
import com.example.bookstore.resources.CartResource;
import com.example.bookstore.resources.CustomerBaseResource;
import com.example.bookstore.resources.CustomerIdResource;
import com.example.bookstore.resources.CustomerOrderResource;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("api")
public class MyApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        // Resources
        classes.add(AuthorIdResource.class);
        classes.add(AuthorBaseResource.class);
        classes.add(BookBaseResource.class);
        classes.add(BookIdResource.class);
        classes.add(CustomerBaseResource.class);
        classes.add(CustomerIdResource.class);
        classes.add(CartResource.class);
        classes.add(CustomerOrderResource.class);

        // Filters
        classes.add(TrailingSlashFilter.class);
        classes.add(RequestLoggingFilter.class);
        classes.add(ResponseLoggingFilter.class);

        // Exception mappers
        classes.add(BookNotFoundExceptionMapper.class);
        classes.add(AuthorNotFoundExceptionMapper.class);
        classes.add(CustomerNotFoundExceptionMapper.class);
        classes.add(InvalidInputExceptionMapper.class);
        classes.add(OutOfStockExceptionMapper.class);
        classes.add(OrderNotFoundExceptionMapper.class);
        classes.add(CartItemExistsExceptionMapper.class);
        classes.add(CartItemNotFoundExceptionMapper.class);
        classes.add(CartItemsNotFoundExceptionMapper.class);
        classes.add(UnrecognizedFieldExceptionMapper.class);

        classes.add(GenericExceptionMapper.class);
        classes.add(JsonProcessingExceptionMapper.class);
        classes.add(JsonProcessingExceptionMapper.class);
        classes.add(WebApplicationExceptionMapper.class);
        
        // Authentication
        classes.add(AuthResource.class);
        classes.add(JwtAuthenticationFilter.class);

        classes.add(JacksonFeature.class);

        return classes;
    }
}
