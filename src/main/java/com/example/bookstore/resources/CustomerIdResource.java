package com.example.bookstore.resources;

import com.example.bookstore.config.AppConfig;
import com.example.bookstore.exception.InvalidInputException;
import com.example.bookstore.models.Customer;
import com.example.bookstore.models.DeleteResponse;
import com.example.bookstore.models.UpdateResponse;
import com.example.bookstore.repository.CustomerRepository;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/customers/{id}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerIdResource {

    private static final Logger LOGGER = Logger.getLogger(CustomerIdResource.class.getName());
    private CustomerRepository customerRepository = AppConfig.getCustomerRepository();

    @GET
    public Customer getCustomerById(@PathParam("id") String idString) {
        LOGGER.log(Level.INFO, "REST - Getting customer by ID: {0}", idString);
        Long id = validateAndParseId(idString);
        Customer customer = customerRepository.getCustomerById(id);
        LOGGER.log(Level.INFO, "REST - Retrieved customer: {0}", customer.getName());
        return customer;
    }

    @PUT
    public Response updateCustomer(@PathParam("id") String idString, Customer customer) {
        LOGGER.log(Level.INFO, "REST - Updating customer with ID: {0}", idString);
        Long id = validateAndParseId(idString);
        // Repository will check for duplicate emails on update
        UpdateResponse<Customer> updateResponse = customerRepository.updateCustomer(id, customer);
        LOGGER.log(Level.INFO, "REST - Customer updated successfully. Fields updated: {0}", updateResponse.getFieldsUpdated());
        return Response.ok(updateResponse).build();
    }

    @DELETE
    public Response deleteCustomer(@PathParam("id") String idString) {
        LOGGER.log(Level.INFO, "REST - Deleting customer with ID: {0}", idString);

        if (idString == null || idString.trim().isEmpty()) {
            LOGGER.warning("REST - Delete customer failed: ID is empty");
            throw new InvalidInputException("Customer ID cannot be empty.");
        }

        Long id = validateAndParseId(idString);
        DeleteResponse deleteResponse = customerRepository.deleteCustomer(id);
        LOGGER.log(Level.INFO, "REST - Customer deleted successfully with ID: {0}", id);
        return Response.ok(deleteResponse).build();
    }

    private Long validateAndParseId(String idString) {
        if (idString == null || idString.trim().isEmpty()) {
            LOGGER.warning("REST - Customer ID is empty");
            throw new InvalidInputException("Customer ID cannot be empty.");
        }

        try {
            Long id = Long.valueOf(idString.trim());
            LOGGER.log(Level.FINE, "REST - Parsed customer ID: {0}", id);
            return id;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "REST - Invalid customer ID format: {0}", idString);
            throw new InvalidInputException("Customer ID must be a valid number.");
        }
    }
}
