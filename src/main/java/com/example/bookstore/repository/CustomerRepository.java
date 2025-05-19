package com.example.bookstore.repository;

import com.example.bookstore.exception.CustomerNotFoundException;
import com.example.bookstore.exception.InvalidInputException;
import com.example.bookstore.models.Customer;
import com.example.bookstore.models.DeleteResponse;
import com.example.bookstore.models.UpdateResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class CustomerRepository {

    private static final Map<Long, Customer> customers = new ConcurrentHashMap<>();
    private static final AtomicLong customerIdCounter = new AtomicLong(1);

    private CartRepository cartRepository;

    public void setCartRepository(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    // Create
    public synchronized Customer addCustomer(Customer customer) {
        if (customer.getName().trim().isEmpty()) {
            throw new InvalidInputException("Customer name cannot be empty.");
        }

        // Validate email
        if (!customer.getEmail().contains("@")) {
            throw new InvalidInputException("Email must be valid.");
        }

        // Validate password
        if (customer.getPassword().length() < 6) {
            throw new InvalidInputException("Password must be at least 6 characters.");
        }

        // Check for duplicate email
        if (isDuplicateEmail(customer.getEmail())) {
            throw new InvalidInputException("A customer with this email already exists.");
        }

        customer.setId(customerIdCounter.getAndIncrement());
        customers.put(customer.getId(), customer);
        return customer;
    }

    // Check if a customer with the same email already exists
    public synchronized boolean isDuplicateEmail(String email) {
        if (email == null) {
            return false;
        }

        String normalizedEmail = normalizeEmail(email);

        return customers.values().stream()
                .anyMatch(existingCustomer -> normalizeEmail(existingCustomer.getEmail()).equals(normalizedEmail));
    }

    // Normalize email by converting to lowercase
    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }

        // Convert email to lowercase for case-insensitive comparison
        return email.toLowerCase().trim();
    }

    // Read
    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customers.values());
    }

    public Customer getCustomerById(Long id) {
        Customer customer = customers.get(id);
        if (customer == null) {
            throw new CustomerNotFoundException(id);
        }
        return customer;
    }

    // Update
    public synchronized UpdateResponse<Customer> updateCustomer(Long id, Customer updatedCustomer) {
        Customer currentCustomer = getCustomerById(id);
        int fieldsUpdated = 0;
        boolean updated = false;

        // Validate and update email
        if (updatedCustomer.getEmail() != null) {
            if (!updatedCustomer.getEmail().contains("@")) {
                throw new InvalidInputException("Email must be valid.");
            }

            // Check if the update would create a duplicate email
            if (!normalizeEmail(currentCustomer.getEmail()).equals(normalizeEmail(updatedCustomer.getEmail()))
                    && isDuplicateEmail(updatedCustomer.getEmail())) {
                throw new InvalidInputException("Email address already in use by another customer.");
            } else if (!currentCustomer.getEmail().equals(updatedCustomer.getEmail())) {
                currentCustomer.setEmail(updatedCustomer.getEmail());
                fieldsUpdated++;
                updated = true;
            }
        }

        // Validate and update name
        if (updatedCustomer.getName() != null) {
            if (updatedCustomer.getName().trim().isEmpty()) {
                throw new InvalidInputException("Customer name cannot be empty.");
            } else if (!currentCustomer.getName().equals(updatedCustomer.getName())) {
                currentCustomer.setName(updatedCustomer.getName());
                fieldsUpdated++;
                updated = true;
            }
        }

        // Validate and update password
        if (updatedCustomer.getPassword() != null && !updatedCustomer.getPassword().trim().isEmpty()) {
            if (updatedCustomer.getPassword().length() < 6) {
                throw new InvalidInputException("Password must be at least 6 characters.");
            } else if (!currentCustomer.getPassword().equals(updatedCustomer.getPassword())) {
                currentCustomer.setPassword(updatedCustomer.getPassword());
                fieldsUpdated++;
                updated = true;
            }
        }

        return new UpdateResponse<>(currentCustomer, updated, fieldsUpdated);
    }

    // Delete
    public synchronized DeleteResponse deleteCustomer(Long id) {
        if (!exists(id)) {
            throw new CustomerNotFoundException(id);
        }

        customers.remove(id);
        cartRepository.deleteCart(id);
        return new DeleteResponse(true, 1);
    }

    // Utility methods
    public boolean exists(Long id) {
        return customers.containsKey(id);
    }
}
