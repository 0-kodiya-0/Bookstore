package com.example.bookstore.repository;

import com.example.bookstore.config.JPAConfig;
import com.example.bookstore.exception.CartItemExistsException;
import com.example.bookstore.exception.CartItemNotFoundException;
import com.example.bookstore.exception.CartItemsNotFoundException;
import com.example.bookstore.exception.CustomerNotFoundException;
import com.example.bookstore.exception.InvalidInputException;
import com.example.bookstore.exception.OutOfStockException;
import com.example.bookstore.models.Book;
import com.example.bookstore.models.Cart;
import com.example.bookstore.models.CartItem;
import com.example.bookstore.models.Customer;
import com.example.bookstore.models.DeleteResponse;
import com.example.bookstore.models.UpdateResponse;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

public class CartRepository {

    private static final Logger LOGGER = Logger.getLogger(CartRepository.class.getName());
    private CustomerRepository customerRepository;
    private BookRepository bookRepository;

    public void setBookRepository(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public void setCustomerRepository(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // Create or add item to cart
    public Cart addOrCreateCart(Long customerId, CartItem item) {
        return JPAConfig.executeInTransaction(em -> {
            // Verify customer exists
            Customer customer = em.find(Customer.class, customerId);
            if (customer == null) {
                throw new CustomerNotFoundException(customerId);
            }

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new InvalidInputException("Quantity must be positive and greater than 0.");
            }

            // Verify book exists and check stock
            Book book = em.find(Book.class, item.getBookId());
            if (book == null) {
                throw new InvalidInputException("Book with ID " + item.getBookId() + " does not exist.");
            }

            if (book.getStock() < item.getQuantity()) {
                throw new OutOfStockException(book.getId(), item.getQuantity(), book.getStock());
            }

            // Check if item already exists in cart
            TypedQuery<CartItem> query = em.createQuery(
                "SELECT ci FROM CartItem ci WHERE ci.customer.id = :customerId AND ci.book.id = :bookId", 
                CartItem.class
            );
            query.setParameter("customerId", customerId);
            query.setParameter("bookId", item.getBookId());

            List<CartItem> existingItems = query.getResultList();
            if (!existingItems.isEmpty()) {
                throw new CartItemExistsException(item.getBookId());
            }

            // Create new cart item
            CartItem newCartItem = new CartItem();
            newCartItem.setCustomer(customer);
            newCartItem.setBook(book);
            newCartItem.setQuantity(item.getQuantity());

            em.persist(newCartItem);

            LOGGER.log(Level.INFO, "Cart item added for customer ID: {0}, book ID: {1}", 
                    new Object[]{customerId, item.getBookId()});

            // Return updated cart
            return getCartByCustomerId(customerId, em);
        });
    }

    // Read cart by customer ID
    public Cart getCartByCustomerId(Long customerId) {
        return JPAConfig.executeReadOnly(em -> {
            return getCartByCustomerId(customerId, em);
        });
    }

    private Cart getCartByCustomerId(Long customerId, EntityManager em) {
        // Verify customer exists
        Customer customer = em.find(Customer.class, customerId);
        if (customer == null) {
            throw new CustomerNotFoundException(customerId);
        }

        // Get cart items for customer
        TypedQuery<CartItem> query = em.createQuery(
            "SELECT ci FROM CartItem ci WHERE ci.customer.id = :customerId", 
            CartItem.class
        );
        query.setParameter("customerId", customerId);

        List<CartItem> cartItems = query.getResultList();
        
        LOGGER.log(Level.INFO, "Retrieved {0} cart items for customer ID: {1}", 
                new Object[]{cartItems.size(), customerId});

        return new Cart(customerId, cartItems);
    }

    // Update cart item quantity
    public UpdateResponse<Cart> updateCartItemQuantity(Long customerId, Long bookId, CartItem item) {
        return JPAConfig.executeInTransaction(em -> {
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new InvalidInputException("Quantity must be positive and greater than 0.");
            }

            // Verify customer exists
            Customer customer = em.find(Customer.class, customerId);
            if (customer == null) {
                throw new CustomerNotFoundException(customerId);
            }

            // Verify book exists
            Book book = em.find(Book.class, bookId);
            if (book == null) {
                throw new InvalidInputException("Book with ID " + bookId + " does not exist.");
            }

            // Check stock
            if (book.getStock() < item.getQuantity()) {
                throw new OutOfStockException(book.getId(), item.getQuantity(), book.getStock());
            }

            // Find existing cart item
            TypedQuery<CartItem> query = em.createQuery(
                "SELECT ci FROM CartItem ci WHERE ci.customer.id = :customerId AND ci.book.id = :bookId", 
                CartItem.class
            );
            query.setParameter("customerId", customerId);
            query.setParameter("bookId", bookId);

            List<CartItem> cartItems = query.getResultList();
            if (cartItems.isEmpty()) {
                throw new CartItemNotFoundException(customerId, bookId);
            }

            CartItem existingItem = cartItems.get(0);
            existingItem.setQuantity(item.getQuantity());
            em.merge(existingItem);

            LOGGER.log(Level.INFO, "Cart item quantity updated for customer ID: {0}, book ID: {1}", 
                    new Object[]{customerId, bookId});

            Cart updatedCart = getCartByCustomerId(customerId, em);
            return new UpdateResponse<>(updatedCart, true, 1);
        });
    }

    // Remove item from cart
    public DeleteResponse removeCartItem(Long customerId, Long bookId) {
        return JPAConfig.executeInTransaction(em -> {
            // Verify customer exists
            Customer customer = em.find(Customer.class, customerId);
            if (customer == null) {
                throw new CustomerNotFoundException(customerId);
            }

            // Find cart item to remove
            TypedQuery<CartItem> query = em.createQuery(
                "SELECT ci FROM CartItem ci WHERE ci.customer.id = :customerId AND ci.book.id = :bookId", 
                CartItem.class
            );
            query.setParameter("customerId", customerId);
            query.setParameter("bookId", bookId);

            List<CartItem> cartItems = query.getResultList();
            if (cartItems.isEmpty()) {
                throw new CartItemNotFoundException(customerId, bookId);
            }

            CartItem itemToRemove = cartItems.get(0);
            em.remove(itemToRemove);

            LOGGER.log(Level.INFO, "Cart item removed for customer ID: {0}, book ID: {1}", 
                    new Object[]{customerId, bookId});

            return new DeleteResponse(true, 1);
        });
    }

    // Delete entire cart for customer
    public void deleteCart(Long customerId) {
        JPAConfig.executeInTransaction(em -> {
            // Delete all cart items for customer
            int deletedCount = em.createQuery(
                "DELETE FROM CartItem ci WHERE ci.customer.id = :customerId"
            ).setParameter("customerId", customerId).executeUpdate();

            LOGGER.log(Level.INFO, "Deleted {0} cart items for customer ID: {1}", 
                    new Object[]{deletedCount, customerId});
        });
    }

    // Get all cart items for customer (for order processing)
    public List<CartItem> getCartItems(Long customerId) {
        return JPAConfig.executeReadOnly(em -> {
            // Verify customer exists
            Customer customer = em.find(Customer.class, customerId);
            if (customer == null) {
                throw new CustomerNotFoundException(customerId);
            }

            TypedQuery<CartItem> query = em.createQuery(
                "SELECT ci FROM CartItem ci WHERE ci.customer.id = :customerId", 
                CartItem.class
            );
            query.setParameter("customerId", customerId);

            List<CartItem> items = query.getResultList();
            if (items.isEmpty()) {
                throw new CartItemsNotFoundException(customerId);
            }

            return items;
        });
    }
}