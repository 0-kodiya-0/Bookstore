package com.example.bookstore.resources;

import com.example.bookstore.config.AppConfig;
import com.example.bookstore.exception.InvalidInputException;
import com.example.bookstore.models.Customer;
import com.example.bookstore.repository.CustomerRepository;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerBaseResource {

    private static final Logger LOGGER = Logger.getLogger(CustomerBaseResource.class.getName());
    private CustomerRepository customerRepository = AppConfig.getCustomerRepository();

    @POST
    public Response createCustomer(Customer customer, @Context UriInfo uriInfo) {
        LOGGER.log(Level.INFO, "REST - Creating customer with email: {0}", customer.getEmail());
        
        if (customer.getName() == null) {
            LOGGER.warning("REST - Create customer failed: Name is empty");
            throw new InvalidInputException("Customer name cannot be empty.");
        }

        // Validate email
        if (customer.getEmail() == null) {
            LOGGER.warning("REST - Create customer failed: Email is empty");
            throw new InvalidInputException("Email cannot be empty.");
        }

        // Validate password
        if (customer.getPassword() == null) {
            LOGGER.warning("REST - Create customer failed: Password is empty");
            throw new InvalidInputException("Password must be at least 6 characters.");
        }

        // CustomerRepository will check for duplicate emails internally
        Customer createdCustomer = customerRepository.addCustomer(customer);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(createdCustomer.getId())).build();

        LOGGER.log(Level.INFO, "REST - Customer created successfully with ID: {0}", createdCustomer.getId());
        return Response.created(location).entity(createdCustomer).build();
    }

    @GET
    public List<Customer> getAllCustomers() {
        LOGGER.info("REST - Getting all customers");
        List<Customer> customers = customerRepository.getAllCustomers();
        LOGGER.log(Level.INFO, "REST - Retrieved {0} customers", customers.size());
        return customers;
    }
}
