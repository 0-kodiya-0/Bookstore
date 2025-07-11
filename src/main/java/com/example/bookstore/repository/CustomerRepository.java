package com.example.bookstore.repository;

import com.example.bookstore.config.JPAConfig;
import com.example.bookstore.exception.CustomerNotFoundException;
import com.example.bookstore.exception.InvalidInputException;
import com.example.bookstore.models.Customer;
import com.example.bookstore.models.DeleteResponse;
import com.example.bookstore.models.UpdateResponse;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

public class CustomerRepository {

    private static final Logger LOGGER = Logger.getLogger(CustomerRepository.class.getName());
    private CartRepository cartRepository;

    public void setCartRepository(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    // Create
    public Customer addCustomer(Customer customer) {
        return JPAConfig.executeInTransaction(em -> {
            // Validate fields
            if (customer.getName() == null || customer.getName().trim().isEmpty()) {
                throw new InvalidInputException("Customer name cannot be empty.");
            }

            if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
                throw new InvalidInputException("Email cannot be empty.");
            }

            if (!customer.getEmail().contains("@")) {
                throw new InvalidInputException("Email must be valid.");
            }

            if (customer.getPassword() == null || customer.getPassword().length() < 6) {
                throw new InvalidInputException("Password must be at least 6 characters.");
            }

            // Check for duplicate email
            if (isDuplicateEmail(customer.getEmail(), em)) {
                throw new InvalidInputException("A customer with this email already exists.");
            }

            em.persist(customer);
            LOGGER.log(Level.INFO, "Customer created with ID: {0}", customer.getId());
            return customer;
        });
    }

    // Check if a customer with the same email already exists
    private boolean isDuplicateEmail(String email, EntityManager em) {
        if (email == null) {
            return false;
        }

        String normalizedEmail = normalizeEmail(email);

        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(c) FROM Customer c WHERE LOWER(c.email) = :normalizedEmail", 
            Long.class
        );
        query.setParameter("normalizedEmail", normalizedEmail);
        
        return query.getSingleResult() > 0;
    }

    // Normalize email by converting to lowercase
    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.toLowerCase().trim();
    }

    // Read
    public List<Customer> getAllCustomers() {
        return JPAConfig.executeReadOnly(em -> {
            TypedQuery<Customer> query = em.createQuery("SELECT c FROM Customer c ORDER BY c.name", Customer.class);
            List<Customer> customers = query.getResultList();
            LOGGER.log(Level.INFO, "Retrieved {0} customers", customers.size());
            return customers;
        });
    }

    public Customer getCustomerById(Long id) {
        return JPAConfig.executeReadOnly(em -> {
            Customer customer = em.find(Customer.class, id);
            if (customer == null) {
                throw new CustomerNotFoundException(id);
            }
            LOGGER.log(Level.INFO, "Retrieved customer: {0}", customer.getName());
            return customer;
        });
    }

    // Update
    public UpdateResponse<Customer> updateCustomer(Long id, Customer updatedCustomer) {
        return JPAConfig.executeInTransaction(em -> {
            Customer currentCustomer = em.find(Customer.class, id);
            if (currentCustomer == null) {
                throw new CustomerNotFoundException(id);
            }

            int fieldsUpdated = 0;
            boolean updated = false;

            // Update email
            if (updatedCustomer.getEmail() != null) {
                if (!updatedCustomer.getEmail().contains("@")) {
                    throw new InvalidInputException("Email must be valid.");
                }

                // Check if the update would create a duplicate email
                if (!normalizeEmail(currentCustomer.getEmail()).equals(normalizeEmail(updatedCustomer.getEmail()))) {
                    TypedQuery<Long> query = em.createQuery(
                        "SELECT COUNT(c) FROM Customer c WHERE c.id != :id AND LOWER(c.email) = :normalizedEmail", 
                        Long.class
                    );
                    query.setParameter("id", id);
                    query.setParameter("normalizedEmail", normalizeEmail(updatedCustomer.getEmail()));
                    
                    if (query.getSingleResult() > 0) {
                        throw new InvalidInputException("Email address already in use by another customer.");
                    }
                }
                
                if (!currentCustomer.getEmail().equals(updatedCustomer.getEmail())) {
                    currentCustomer.setEmail(updatedCustomer.getEmail());
                    fieldsUpdated++;
                    updated = true;
                }
            }

            // Update name
            if (updatedCustomer.getName() != null) {
                if (updatedCustomer.getName().trim().isEmpty()) {
                    throw new InvalidInputException("Customer name cannot be empty.");
                }
                
                if (!currentCustomer.getName().equals(updatedCustomer.getName())) {
                    currentCustomer.setName(updatedCustomer.getName());
                    fieldsUpdated++;
                    updated = true;
                }
            }

            // Update password
            if (updatedCustomer.getPassword() != null && !updatedCustomer.getPassword().trim().isEmpty()) {
                if (updatedCustomer.getPassword().length() < 6) {
                    throw new InvalidInputException("Password must be at least 6 characters.");
                }
                
                if (!currentCustomer.getPassword().equals(updatedCustomer.getPassword())) {
                    currentCustomer.setPassword(updatedCustomer.getPassword());
                    fieldsUpdated++;
                    updated = true;
                }
            }

            if (updated) {
                em.merge(currentCustomer);
                LOGGER.log(Level.INFO, "Customer updated with ID: {0}, fields updated: {1}", new Object[]{id, fieldsUpdated});
            }

            return new UpdateResponse<>(currentCustomer, updated, fieldsUpdated);
        });
    }

    // Delete
    public DeleteResponse deleteCustomer(Long id) {
        return JPAConfig.executeInTransaction(em -> {
            Customer customer = em.find(Customer.class, id);
            if (customer == null) {
                throw new CustomerNotFoundException(id);
            }

            // Delete associated cart items first (cascade should handle this, but explicit for clarity)
            em.createQuery("DELETE FROM CartItem ci WHERE ci.customer.id = :customerId")
                .setParameter("customerId", id)
                .executeUpdate();

            em.remove(customer);
            LOGGER.log(Level.INFO, "Customer deleted with ID: {0}", id);
            return new DeleteResponse(true, 1);
        });
    }

    // Utility methods
    public boolean exists(Long id) {
        return JPAConfig.executeReadOnly(em -> {
            return em.find(Customer.class, id) != null;
        });
    }
    
    // Find customer by email (for authentication)
    public Customer findByEmail(String email) {
        return JPAConfig.executeReadOnly(em -> {
            TypedQuery<Customer> query = em.createQuery(
                "SELECT c FROM Customer c WHERE LOWER(c.email) = LOWER(:email)", 
                Customer.class
            );
            query.setParameter("email", email);
            
            try {
                Customer customer = query.getSingleResult();
                LOGGER.log(Level.INFO, "Found customer by email: {0}", email);
                return customer;
            } catch (Exception e) {
                LOGGER.log(Level.INFO, "No customer found with email: {0}", email);
                return null;
            }
        });
    }
    
    // Search functionality
    public List<Customer> searchCustomers(String searchTerm) {
        return JPAConfig.executeReadOnly(em -> {
            TypedQuery<Customer> query = em.createQuery(
                "SELECT c FROM Customer c WHERE LOWER(c.name) LIKE LOWER(:searchTerm) OR LOWER(c.email) LIKE LOWER(:searchTerm) ORDER BY c.name", 
                Customer.class
            );
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            
            List<Customer> customers = query.getResultList();
            LOGGER.log(Level.INFO, "Found {0} customers matching search term: {1}", new Object[]{customers.size(), searchTerm});
            return customers;
        });
    }
}