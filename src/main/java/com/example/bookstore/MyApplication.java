/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.bookstore;

import com.example.bookstore.exception.mapper.AuthorNotFoundExceptionMapper;
import com.example.bookstore.exception.mapper.BookNotFoundExceptionMapper;
import com.example.bookstore.exception.mapper.CartItemExistsExceptionMapper;
import com.example.bookstore.exception.mapper.CartItemNotFoundExceptionMapper;
import com.example.bookstore.exception.mapper.CartItemsNotFoundExceptionMapper;
import com.example.bookstore.exception.mapper.CustomerNotFoundExceptionMapper;
import com.example.bookstore.exception.mapper.InvalidInputExceptionMapper;
import com.example.bookstore.exception.mapper.OrderNotFoundExceptionMapper;
import com.example.bookstore.exception.mapper.OutOfStockExceptionMapper;
import com.example.bookstore.exception.mapper.UnrecognizedFieldExceptionMapper;
import com.example.bookstore.resources.AuthorResource;
import com.example.bookstore.resources.BookResource;
import com.example.bookstore.resources.CartResource;
import com.example.bookstore.resources.CustomerOrderResource;
import com.example.bookstore.resources.CustomerResource;
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
        classes.add(BookResource.class);
        classes.add(AuthorResource.class);
        classes.add(CustomerResource.class);
        classes.add(CartResource.class);
        classes.add(CustomerOrderResource.class);

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

        return classes;
    }
}
