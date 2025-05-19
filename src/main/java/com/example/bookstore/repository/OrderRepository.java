package com.example.bookstore.repository;

import com.example.bookstore.exception.CustomerNotFoundException;
import com.example.bookstore.exception.InvalidInputException;
import com.example.bookstore.exception.OutOfStockException;
import com.example.bookstore.models.Book;
import com.example.bookstore.models.Cart;
import com.example.bookstore.models.CartItem;
import com.example.bookstore.models.Order;
import com.example.bookstore.models.OrderItem;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class OrderRepository {

    private static final Map<Long, Order> orders = new ConcurrentHashMap<>();
    private static final AtomicLong orderIdCounter = new AtomicLong(1);

    private CustomerRepository customerRepository;
    private CartRepository cartRepository;
    private BookRepository bookRepository;

    public void setBookRepository(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public void setCustomerRepository(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public void setCartRepository(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    // Create
    public synchronized Order createOrder(Long customerId) {
        // Verify customer exists
        if (!customerRepository.exists(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }

        Cart cart = cartRepository.getCartByCustomerId(customerId);

        // Validate cart is not empty
        if (cart.getItems().isEmpty()) {
            throw new InvalidInputException("Cannot create an order from an empty cart.");
        }

        // Create new order
        Order order = new Order(null, customerId);
        
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        order.setOrderDate(calendar.getTime());

        // Add items to order
        for (CartItem cartItem : cart.getItems()) {
            Book book = bookRepository.getBookById(cartItem.getBookId());

            // Check stock
            if (book.getStock() < cartItem.getQuantity()) {
                throw new OutOfStockException(book.getId(), cartItem.getQuantity(), book.getStock());
            }

            // Add to order
            OrderItem orderItem = new OrderItem(
                    book.getId(),
                    book.getTitle(),
                    cartItem.getQuantity(),
                    book.getPrice()
            );

            order.getItems().add(orderItem);

            // Update book stock
            bookRepository.reduceStock(book.getId(), cartItem.getQuantity());
        }

        // Calculate total
        order.calculateTotal();

        order.setId(orderIdCounter.getAndIncrement());
        orders.put(order.getId(), order);

        // Clear cart
        cartRepository.deleteCart(customerId);

        return order;
    }

    // Read
    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    public Order getOrderById(Long id) {
        return orders.get(id);
    }

    public List<Order> getOrdersByCustomerId(Long customerId) {
        // Verify customer exists
        if (!customerRepository.exists(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }

        return orders.values().stream()
                .filter(order -> order.getCustomerId().equals(customerId))
                .collect(Collectors.toList());
    }

    public Order getCustomerOrderById(Long customerId, Long orderId) {
        // Verify customer exists
        if (!customerRepository.exists(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }

        Order order = orders.get(orderId);
        if (order != null && order.getCustomerId().equals(customerId)) {
            return order;
        }
        return null;
    }
}
