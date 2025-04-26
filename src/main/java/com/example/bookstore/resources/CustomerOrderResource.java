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

/**
 *
 * @author HP
 */
@Path("/customers/{customerId}/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerOrderResource {

    private OrderRepository orderRepository = AppConfig.getOrderRepository();
    
    @GET
    @Path("/{orderId}")
    public Response getCustomerOrder(
            @PathParam("customerId") String customerIdString,
            @PathParam("orderId") String orderIdString) {
        Long customerId = validateAndParseId(customerIdString, "Customer");
        Long orderId = validateAndParseId(orderIdString, "Order");

        Order order = orderRepository.getCustomerOrderById(customerId, orderId);
        if (order == null) {
            throw new OrderNotFoundException(customerId, orderId);
        }

        return Response.ok(order).build();
    }

    @GET
    public List<Order> getCustomerOrders(@PathParam("customerId") String customerIdString) {
        Long customerId = validateAndParseId(customerIdString, "Customer");
        return orderRepository.getOrdersByCustomerId(customerId);
    }

    @POST
    public Response createOrderFromCart(
            @PathParam("customerId") String customerIdString,
            @Context UriInfo uriInfo) {
        Long customerId = validateAndParseId(customerIdString, "Customer");

        // Save order
        Order createdOrder = orderRepository.createOrder(customerId);

        // Return created order
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(createdOrder.getId()))
                .build();

        return Response.created(location).entity(createdOrder).build();
    }

    private Long validateAndParseId(String idString, String entityType) {
        if (idString == null || idString.trim().isEmpty()) {
            throw new InvalidInputException(entityType + " ID cannot be empty.");
        }

        try {
            return Long.parseLong(idString.trim());
        } catch (NumberFormatException e) {
            throw new InvalidInputException(entityType + " ID must be a valid number.");
        }
    }
}
