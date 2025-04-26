package com.example.bookstore.resources;

import com.example.bookstore.config.AppConfig;
import com.example.bookstore.exception.InvalidInputException;
import com.example.bookstore.models.Cart;
import com.example.bookstore.models.CartItem;
import com.example.bookstore.models.DeleteResponse;
import com.example.bookstore.models.UpdateResponse;
import com.example.bookstore.repository.CartRepository;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author HP
 */
@Path("/customers/{customerId}/cart")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CartResource {

    private CartRepository cartRepository = AppConfig.getCartRepository();

    @GET
    public Cart getCart(@PathParam("customerId") String customerIdString) {
        Long customerId = validateAndParseId(customerIdString, "Customer");
        return cartRepository.getCartByCustomerId(customerId);
    }

    @POST
    @Path("/items")
    public Cart addItemToCart(@PathParam("customerId") String customerIdString, CartItem item) {
        Long customerId = validateAndParseId(customerIdString, "Customer");
        
        if (item.getBookId() == null) {
            throw new InvalidInputException("Book ID cannot be empty.");
        }
        return cartRepository.addOrCreateCart(customerId, item);
    }

    @PUT
    @Path("/items/{bookId}")
    public UpdateResponse<Cart> updateCartItem(
            @PathParam("customerId") String customerIdString,
            @PathParam("bookId") String bookIdString, CartItem item) {
        Long customerId = validateAndParseId(customerIdString, "Customer");
        Long bookId = validateAndParseId(bookIdString, "Book");
        
        return cartRepository.updateCartItemQuantity(customerId, bookId, item);
    }

    @DELETE
    @Path("/items/{bookId}")
    public DeleteResponse removeCartItem(
            @PathParam("customerId") String customerIdString,
            @PathParam("bookId") String bookIdString) {
        Long customerId = validateAndParseId(customerIdString, "Customer");
        Long bookId = validateAndParseId(bookIdString, "Book");
        
        return cartRepository.removeCartItem(customerId, bookId);
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