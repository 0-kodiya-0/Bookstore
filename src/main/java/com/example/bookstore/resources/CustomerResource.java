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
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

/**
 *
 * @author HP
 */
@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    private CustomerRepository customerRepository = AppConfig.getCustomerRepository();

    @POST
    public Response createCustomer(Customer customer, @Context UriInfo uriInfo) {
        if (customer.getName() == null) {
            throw new InvalidInputException("Customer name cannot be empty.");
        }

        // Validate email
        if (customer.getEmail() == null) {
            throw new InvalidInputException("Email cannot be empty.");
        }

        // Validate password
        if (customer.getPassword() == null) {
            throw new InvalidInputException("Password must be at least 6 characters.");
        }

        // CustomerRepository will check for duplicate emails internally
        Customer createdCustomer = customerRepository.addCustomer(customer);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(createdCustomer.getId())).build();

        return Response.created(location).entity(createdCustomer).build();
    }

    @GET
    public List<Customer> getAllCustomers() {
        return customerRepository.getAllCustomers();
    }

    @GET
    @Path("/{id}")
    public Customer getCustomerById(@PathParam("id") Long id) {
        return customerRepository.getCustomerById(id);
    }

    @PUT
    @Path("/{id}")
    public Response updateCustomer(@PathParam("id") Long id, Customer customer) {
        // Repository will check for duplicate emails on update
        UpdateResponse<Customer> updateResponse = customerRepository.updateCustomer(id, customer);
        return Response.ok(updateResponse).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteCustomer(@PathParam("id") Long id) {
        DeleteResponse deleteResponse = customerRepository.deleteCustomer(id);
        return Response.ok(deleteResponse).build();
    }
}
