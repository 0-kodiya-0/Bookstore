package com.example.bookstore.repository;

import com.example.bookstore.exception.CartItemExistsException;
import com.example.bookstore.exception.CartItemNotFoundException;
import com.example.bookstore.exception.CartItemsNotFoundException;
import com.example.bookstore.exception.CustomerNotFoundException;
import com.example.bookstore.exception.InvalidInputException;
import com.example.bookstore.exception.OutOfStockException;
import com.example.bookstore.models.Book;
import com.example.bookstore.models.Cart;
import com.example.bookstore.models.CartItem;
import com.example.bookstore.models.DeleteResponse;
import com.example.bookstore.models.UpdateResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author HP
 */
public class CartRepository {

    private static final Map<Long, Cart> carts = new ConcurrentHashMap<>();
    private CustomerRepository customerRepository;
    private BookRepository bookRepository;

    public void setBookRepository(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public void setCustomerRepository(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // Create or get
    public synchronized Cart addOrCreateCart(Long customerId, CartItem item) {
        // Verify customer exists
        if (!customerRepository.exists(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }

        // Validate quantity
        if (item.getQuantity() <= 0) {
            throw new InvalidInputException("Quantity cannot be negative or equal to 0.");
        }

        Book book = bookRepository.getBookById(item.getBookId());

        // Check stock
        if (book.getStock() < item.getQuantity()) {
            throw new OutOfStockException(book.getId(), item.getQuantity(), book.getStock());
        }

        Cart cart = carts.get(customerId);
        if (cart == null) {
            cart = new Cart(customerId);
            cart.addItem(item);
            carts.put(customerId, cart);
        } else {
            if (cart.getItems(item.getBookId()) != null) {
                throw new CartItemExistsException(item.getBookId());
            } else {
                cart.addItem(item);
            }
        }

        return cart;
    }

    // Read
    public Cart getCartByCustomerId(Long customerId) {
        // Verify customer exists
        if (!customerRepository.exists(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }

        Cart cart = carts.get(customerId);
        if (cart == null) {
            return new Cart(customerId);
        }
        return cart;
    }

    public synchronized UpdateResponse<Cart> updateCartItemQuantity(Long customerId, Long bookId, CartItem item) {
        if (item.getQuantity() <= 0) {
            throw new InvalidInputException("Quantity must be positive or greater than 0.");
        }

        // Verify customer exists
        Cart cart = getCartByCustomerId(customerId);

        if (cart.getItems().size() <= 0) {
            throw new CartItemsNotFoundException(customerId);
        }

        // Verify book exists
        Book book = bookRepository.getBookById(bookId);

        if (cart.getItems(bookId) == null) {
            throw new CartItemNotFoundException(customerId, bookId);
        }

        // Check stock
        if (book.getStock() < item.getQuantity()) {
            throw new OutOfStockException(book.getId(), item.getQuantity(), book.getStock());
        }

        // Update cart
        cart.updateItem(bookId, item.getQuantity());

        return new UpdateResponse<>(cart, true, 1);
    }

    public synchronized DeleteResponse removeCartItem(Long customerId, Long bookId) {
        // Verify book exists
        if (!customerRepository.exists(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }

        // Remove from cart
        Cart cart = getCartByCustomerId(customerId);

        if (cart.getItems().size() <= 0) {
            throw new CartItemsNotFoundException(customerId);
        }

        if (cart.getItems(bookId) == null) {
            throw new CartItemNotFoundException(customerId, bookId);
        }

        cart.removeItem(bookId);

        return new DeleteResponse(true, 1);
    }

    // Delete
    public synchronized void deleteCart(Long customerId) {
        if (carts.get(customerId).getItems().size() <= 0) {
            return;
        }
        carts.remove(customerId);
    }
}
