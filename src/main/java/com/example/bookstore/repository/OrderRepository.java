package com.example.bookstore.repository;

import com.example.bookstore.config.JPAConfig;
import com.example.bookstore.exception.CustomerNotFoundException;
import com.example.bookstore.exception.InvalidInputException;
import com.example.bookstore.exception.OrderNotFoundException;
import com.example.bookstore.exception.OutOfStockException;
import com.example.bookstore.models.Book;
import com.example.bookstore.models.CartItem;
import com.example.bookstore.models.Customer;
import com.example.bookstore.models.Order;
import com.example.bookstore.models.OrderItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

public class OrderRepository {

    private static final Logger LOGGER = Logger.getLogger(OrderRepository.class.getName());
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

    // Create order from cart
    public Order createOrder(Long customerId) {
        return JPAConfig.executeInTransaction(em -> {
            // Verify customer exists
            Customer customer = em.find(Customer.class, customerId);
            if (customer == null) {
                throw new CustomerNotFoundException(customerId);
            }

            // Get cart items
            TypedQuery<CartItem> cartQuery = em.createQuery(
                "SELECT ci FROM CartItem ci WHERE ci.customer.id = :customerId", 
                CartItem.class
            );
            cartQuery.setParameter("customerId", customerId);
            List<CartItem> cartItems = cartQuery.getResultList();

            // Validate cart is not empty
            if (cartItems.isEmpty()) {
                throw new InvalidInputException("Cannot create an order from an empty cart.");
            }

            // Create new order
            Order order = new Order();
            order.setCustomer(customer);
            order.setOrderDate(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime());

            // Process each cart item
            for (CartItem cartItem : cartItems) {
                Book book = cartItem.getBook();

                // Check stock
                if (book.getStock() < cartItem.getQuantity()) {
                    throw new OutOfStockException(book.getId(), cartItem.getQuantity(), book.getStock());
                }

                // Create order item
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setBook(book);
                orderItem.setBookTitle(book.getTitle());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setPrice(book.getPrice());

                order.addOrderItem(orderItem);

                // Update book stock
                book.setStock(book.getStock() - cartItem.getQuantity());
                em.merge(book);
            }

            // Calculate total with proper rounding
            calculateTotal(order);

            // Persist order (cascade will save order items)
            em.persist(order);

            // Clear cart items
            em.createQuery("DELETE FROM CartItem ci WHERE ci.customer.id = :customerId")
                .setParameter("customerId", customerId)
                .executeUpdate();

            LOGGER.log(Level.INFO, "Order created with ID: {0} for customer ID: {1}", 
                    new Object[]{order.getId(), customerId});

            return order;
        });
    }

    // Get all orders
    public List<Order> getAllOrders() {
        return JPAConfig.executeReadOnly(em -> {
            TypedQuery<Order> query = em.createQuery("SELECT o FROM Order o ORDER BY o.orderDate DESC", Order.class);
            List<Order> orders = query.getResultList();
            LOGGER.log(Level.INFO, "Retrieved {0} orders", orders.size());
            return orders;
        });
    }

    // Get order by ID
    public Order getOrderById(Long id) {
        return JPAConfig.executeReadOnly(em -> {
            Order order = em.find(Order.class, id);
            if (order == null) {
                throw new OrderNotFoundException(id);
            }
            LOGGER.log(Level.INFO, "Retrieved order with ID: {0}", id);
            return order;
        });
    }

    // Get orders by customer ID
    public List<Order> getOrdersByCustomerId(Long customerId) {
        return JPAConfig.executeReadOnly(em -> {
            // Verify customer exists
            Customer customer = em.find(Customer.class, customerId);
            if (customer == null) {
                throw new CustomerNotFoundException(customerId);
            }

            TypedQuery<Order> query = em.createQuery(
                "SELECT o FROM Order o WHERE o.customer.id = :customerId ORDER BY o.orderDate DESC", 
                Order.class
            );
            query.setParameter("customerId", customerId);

            List<Order> orders = query.getResultList();
            LOGGER.log(Level.INFO, "Retrieved {0} orders for customer ID: {1}", 
                    new Object[]{orders.size(), customerId});
            return orders;
        });
    }

    // Get specific customer order by ID
    public Order getCustomerOrderById(Long customerId, Long orderId) {
        return JPAConfig.executeReadOnly(em -> {
            // Verify customer exists
            Customer customer = em.find(Customer.class, customerId);
            if (customer == null) {
                throw new CustomerNotFoundException(customerId);
            }

            // Find order belonging to customer
            TypedQuery<Order> query = em.createQuery(
                "SELECT o FROM Order o WHERE o.id = :orderId AND o.customer.id = :customerId", 
                Order.class
            );
            query.setParameter("orderId", orderId);
            query.setParameter("customerId", customerId);

            List<Order> orders = query.getResultList();
            if (orders.isEmpty()) {
                throw new OrderNotFoundException(customerId, orderId);
            }

            Order order = orders.get(0);
            LOGGER.log(Level.INFO, "Retrieved order ID: {0} for customer ID: {1}", 
                    new Object[]{orderId, customerId});
            return order;
        });
    }

    // Helper method to calculate total with proper rounding to 2 decimal places
    private void calculateTotal(Order order) {
        BigDecimal total = BigDecimal.ZERO;
        
        for (OrderItem item : order.getItems()) {
            BigDecimal price = BigDecimal.valueOf(item.getPrice());
            BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());
            BigDecimal itemTotal = price.multiply(quantity);
            total = total.add(itemTotal);
        }
        
        // Round to exactly 2 decimal places
        total = total.setScale(2, RoundingMode.HALF_EVEN);
        
        order.setTotalAmount(total.doubleValue());
    }

    // Get order statistics
    public OrderStatistics getOrderStatistics() {
        return JPAConfig.executeReadOnly(em -> {
            OrderStatistics stats = new OrderStatistics();

            // Total orders
            Long totalOrders = em.createQuery("SELECT COUNT(o) FROM Order o", Long.class)
                    .getSingleResult();
            stats.totalOrders = totalOrders;

            // Total revenue
            Double totalRevenue = em.createQuery("SELECT SUM(o.totalAmount) FROM Order o", Double.class)
                    .getSingleResult();
            stats.totalRevenue = totalRevenue != null ? totalRevenue : 0.0;

            // Average order value
            if (totalOrders > 0) {
                stats.averageOrderValue = stats.totalRevenue / totalOrders;
            }

            LOGGER.log(Level.INFO, "Order statistics: {0}", stats);
            return stats;
        });
    }

    // Order statistics class
    public static class OrderStatistics {
        public Long totalOrders = 0L;
        public Double totalRevenue = 0.0;
        public Double averageOrderValue = 0.0;

        @Override
        public String toString() {
            return String.format("OrderStatistics{totalOrders=%d, totalRevenue=%.2f, averageOrderValue=%.2f}",
                    totalOrders, totalRevenue, averageOrderValue);
        }
    }
}