package com.example.bookstore.config;

import com.example.bookstore.repository.AuthorRepository;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CartRepository;
import com.example.bookstore.repository.CustomerRepository;
import com.example.bookstore.repository.OrderRepository;
import java.util.logging.Logger;

public class AppConfig {

    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    private static final AuthorRepository authorRepository = new AuthorRepository();
    private static final BookRepository bookRepository = new BookRepository();
    private static final CartRepository cartRepository = new CartRepository();
    private static final CustomerRepository customerRepository = new CustomerRepository();
    private static final OrderRepository orderRepository = new OrderRepository();
    
    static {
        // Initialize logging configuration
        LoggingConfig.initialize();
        LOGGER.info("Logging configuration initialized");
        
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
