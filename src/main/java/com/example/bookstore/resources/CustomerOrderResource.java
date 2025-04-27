package com.example.bookstore.resources;

import com.example.bookstore.config.AppConfig;
import com.example.bookstore.exception.InvalidInputException;
import com.example.bookstore.exception.OrderNotFoundException;
import com.example.bookstore.models.Order;
import com.example.bookstore.repository.OrderRepository;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@Path("/customers/{customerId}/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerOrderResource {

    private static final Logger LOGGER = Logger.getLogger(CustomerOrderResource.class.getName());
    private OrderRepository orderRepository = AppConfig.getOrderRepository();
    
    @GET
    @Path("/{orderId}")
    public Response getCustomerOrder(
            @PathParam("customerId") String customerIdString,
            @PathParam("orderId") String orderIdString) {
        LOGGER.log(Level.INFO, "REST - Getting order ID: {0} for customer ID: {1}", new Object[]{orderIdString, customerIdString});
        Long customerId = validateAndParseId(customerIdString, "Customer");
        Long orderId = validateAndParseId(orderIdString, "Order");

        Order order = orderRepository.getCustomerOrderById(customerId, orderId);
        if (order == null) {
            LOGGER.log(Level.WARNING, "REST - Order not found for customer ID: {0}, order ID: {1}", new Object[]{customerId, orderId});
            throw new OrderNotFoundException(customerId, orderId);
        }

        LOGGER.info("REST - Retrieved order successfully");
        return Response.ok(order).build();
    }

    @GET
    public List<Order> getCustomerOrders(@PathParam("customerId") String customerIdString) {
        LOGGER.log(Level.INFO, "REST - Getting all orders for customer ID: {0}", customerIdString);
        Long customerId = validateAndParseId(customerIdString, "Customer");
        List<Order> orders = orderRepository.getOrdersByCustomerId(customerId);
        LOGGER.log(Level.INFO, "REST - Retrieved {0} orders for customer ID: {1}", new Object[]{orders.size(), customerId});
        return orders;
    }

    @POST
    public Response createOrderFromCart(
            @PathParam("customerId") String customerIdString,
            @Context UriInfo uriInfo) {
        LOGGER.log(Level.INFO, "REST - Creating order from cart for customer ID: {0}", customerIdString);
        Long customerId = validateAndParseId(customerIdString, "Customer");

        // Save order
        Order createdOrder = orderRepository.createOrder(customerId);

        // Return created order
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(createdOrder.getId()))
                .build();

        LOGGER.log(Level.INFO, "REST - Order created successfully with ID: {0} for customer ID: {1}", new Object[]{createdOrder.getId(), customerId});
        return Response.created(location).entity(createdOrder).build();
    }

    private Long validateAndParseId(String idString, String entityType) {
        if (idString == null || idString.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "REST - {0} ID is empty", entityType);
            throw new InvalidInputException(entityType + " ID cannot be empty.");
        }

        try {
            Long id = Long.valueOf(idString.trim());
            LOGGER.log(Level.FINE, "REST - Parsed {0} ID: {1}", new Object[]{entityType, id});
            return id;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "REST - Invalid {0} ID format: {1}", new Object[]{entityType, idString});
            throw new InvalidInputException(entityType + " ID must be a valid number.");
        }
    }
}