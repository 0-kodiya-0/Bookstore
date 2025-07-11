package com.example.bookstore.resources;

import com.example.bookstore.config.AppConfig;
import com.example.bookstore.exception.InvalidInputException;
import com.example.bookstore.models.Customer;
import com.example.bookstore.models.LoginRequest;
import com.example.bookstore.models.LoginResponse;
import com.example.bookstore.models.RegistrationRequest;
import com.example.bookstore.repository.CustomerRepository;
import com.example.bookstore.utils.JwtUtil;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Logger LOGGER = Logger.getLogger(AuthResource.class.getName());
    private final CustomerRepository customerRepository = AppConfig.getCustomerRepository();

    @POST
    @Path("/login")
    public Response login(LoginRequest loginRequest) {
        LOGGER.log(Level.INFO, "REST - Login attempt for email: {0}", loginRequest.getEmail());

        if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
            LOGGER.warning("REST - Login failed: Email is empty");
            throw new InvalidInputException("Email cannot be empty.");
        }

        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            LOGGER.warning("REST - Login failed: Password is empty");
            throw new InvalidInputException("Password cannot be empty.");
        }

        // Find customer by email
        List<Customer> customers = customerRepository.getAllCustomers();
        Customer foundCustomer = null;

        for (Customer customer : customers) {
            if (customer.getEmail().equalsIgnoreCase(loginRequest.getEmail())) {
                foundCustomer = customer;
                break;
            }
        }

        // Validate customer and password
        if (foundCustomer == null || !foundCustomer.getPassword().equals(loginRequest.getPassword())) {
            LOGGER.warning("REST - Login failed: Invalid credentials");
            throw new InvalidInputException("Invalid email or password.");
        }

        // Create a new customer object without password for response
        Customer responseCustomer = new Customer();
        responseCustomer.setId(foundCustomer.getId());
        responseCustomer.setName(foundCustomer.getName());
        responseCustomer.setEmail(foundCustomer.getEmail());

        // Generate JWT token
        String token = JwtUtil.generateToken(
                foundCustomer.getId(),
                foundCustomer.getEmail(),
                foundCustomer.getName());

        // Return response with token and customer details
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setCustomer(responseCustomer);

        LOGGER.log(Level.INFO, "REST - Login successful for email: {0}", loginRequest.getEmail());
        return Response.ok(response).build();
    }

    // Add this method to AuthResource.java
    @POST
    @Path("/register")
    public Response register(RegistrationRequest registrationRequest) {
        LOGGER.log(Level.INFO, "REST - Registration attempt: {0}", registrationRequest);
        
        if (registrationRequest.getName() == null || registrationRequest.getName().trim().isEmpty()) {
            LOGGER.warning("REST - Registration failed: Name is empty");
            throw new InvalidInputException("Name cannot be empty.");
        }

        if (registrationRequest.getEmail() == null || registrationRequest.getEmail().trim().isEmpty()) {
            LOGGER.warning("REST - Registration failed: Email is empty");
            throw new InvalidInputException("Email cannot be empty.");
        }

        if (registrationRequest.getPassword() == null || registrationRequest.getPassword().length() < 6) {
            LOGGER.warning("REST - Registration failed: Password is invalid");
            throw new InvalidInputException("Password must be at least 6 characters.");
        }

        try {
            // Create Customer from RegistrationRequest
            Customer customer = new Customer();
            customer.setName(registrationRequest.getName());
            customer.setEmail(registrationRequest.getEmail());
            customer.setPassword(registrationRequest.getPassword());
            
            // Add customer via repository
            Customer createdCustomer = customerRepository.addCustomer(customer);
            
            // Create response customer without password
            Customer responseCustomer = new Customer();
            responseCustomer.setId(createdCustomer.getId());
            responseCustomer.setName(createdCustomer.getName());
            responseCustomer.setEmail(createdCustomer.getEmail());

            // Generate token for auto-login
            String token = JwtUtil.generateToken(
                    createdCustomer.getId(),
                    createdCustomer.getEmail(),
                    createdCustomer.getName());

            // Create response with token and customer data
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setCustomer(responseCustomer);

            LOGGER.log(Level.INFO, "REST - Registration successful for email: {0}", registrationRequest.getEmail());
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "REST - Registration error: {0}", e.getMessage());
            throw new InvalidInputException("Registration failed: " + e.getMessage());
        }
    }
}
