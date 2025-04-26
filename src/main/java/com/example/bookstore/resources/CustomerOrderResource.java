/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.bookstore.resources;

import com.example.bookstore.config.AppConfig;
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
    public List<Order> getCustomerOrders(@PathParam("customerId") Long customerId) {
        return orderRepository.getOrdersByCustomerId(customerId);
    }

    @GET
    @Path("/{orderId}")
    public Response getCustomerOrder(
            @PathParam("customerId") Long customerId,
            @PathParam("orderId") Long orderId) {
        Order order = orderRepository.getCustomerOrderById(customerId, orderId);
        if (order == null) {
            throw new OrderNotFoundException(customerId, orderId);
        }

        return Response.ok(order).build();
    }

    @POST
    public Response createOrderFromCart(
            @PathParam("customerId") Long customerId,
            @Context UriInfo uriInfo) {
        // Save order
        Order createdOrder = orderRepository.createOrder(customerId);

        // Return created order
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(createdOrder.getId()))
                .build();

        return Response.created(location).entity(createdOrder).build();
    }
}
