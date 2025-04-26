package com.example.bookstore.resources;

import com.example.bookstore.config.AppConfig;
import com.example.bookstore.exception.InvalidInputException;
import com.example.bookstore.models.Customer;
import com.example.bookstore.models.DeleteResponse;
import com.example.bookstore.models.UpdateResponse;
import com.example.bookstore.repository.CustomerRepository;
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

    private CustomerRepository customerRepository = AppConfig.getCustomerRepository();

    @GET
    public Customer getCustomerById(@PathParam("id") String idString) {
        Long id = validateAndParseId(idString);
        return customerRepository.getCustomerById(id);
    }

    @PUT
    public Response updateCustomer(@PathParam("id") String idString, Customer customer) {
        Long id = validateAndParseId(idString);
        // Repository will check for duplicate emails on update
        UpdateResponse<Customer> updateResponse = customerRepository.updateCustomer(id, customer);
        return Response.ok(updateResponse).build();
    }

    @DELETE
    public Response deleteCustomer(@PathParam("id") String idString) {
        // Check if id string is empty (including blank spaces)
        if (idString == null || idString.trim().isEmpty()) {
            throw new InvalidInputException("Customer ID cannot be empty.");
        }
        
        Long id = validateAndParseId(idString);
        DeleteResponse deleteResponse = customerRepository.deleteCustomer(id);
        return Response.ok(deleteResponse).build();
    }

    private Long validateAndParseId(String idString) {
        // This check is redundant in some cases but ensures consistency
        if (idString == null || idString.trim().isEmpty()) {
            throw new InvalidInputException("Customer ID cannot be empty.");
        }
        
        try {
            return Long.parseLong(idString.trim());
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Customer ID must be a valid number.");
        }
    }
}