/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.bookstore.config;

import com.example.bookstore.repository.AuthorRepository;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CartRepository;
import com.example.bookstore.repository.CustomerRepository;
import com.example.bookstore.repository.OrderRepository;

/**
 *
 * @author HP
 */
public class AppConfig {

    private static final AuthorRepository authorRepository = new AuthorRepository();
    private static final BookRepository bookRepository = new BookRepository();
    private static final CartRepository cartRepository = new CartRepository();
    private static final CustomerRepository customerRepository = new CustomerRepository();
    private static final OrderRepository orderRepository = new OrderRepository();
    
    static {
        authorRepository.setBookRepository(bookRepository);
        bookRepository.setAuthorRepository(authorRepository);
        cartRepository.setBookRepository(bookRepository);
        cartRepository.setCustomerRepository(customerRepository);
        customerRepository.setCartRepository(cartRepository);
        orderRepository.setBookRepository(bookRepository);
        orderRepository.setCartRepository(cartRepository);
        orderRepository.setCustomerRepository(customerRepository);
    }

    public static AuthorRepository getAuthorRepository() {
        return authorRepository;
    }

    public static BookRepository getBookRepository() {
        return bookRepository;
    }

    public static CartRepository getCartRepository() {
        return cartRepository;
    }

    public static CustomerRepository getCustomerRepository() {
        return customerRepository;
    }

    public static OrderRepository getOrderRepository() {
        return orderRepository;
    }
}
