package com.example.bookstore.resources;

import com.example.bookstore.config.AppConfig;
import com.example.bookstore.exception.InvalidInputException;
import com.example.bookstore.models.Cart;
import com.example.bookstore.models.CartItem;
import com.example.bookstore.models.DeleteResponse;
import com.example.bookstore.models.UpdateResponse;
import com.example.bookstore.repository.CartRepository;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/customers/{customerId}/cart")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CartResource {

    private static final Logger LOGGER = Logger.getLogger(CartResource.class.getName());
    private CartRepository cartRepository = AppConfig.getCartRepository();

    @GET
    public Cart getCart(@PathParam("customerId") String customerIdString) {
        LOGGER.log(Level.INFO, "REST - Getting cart for customer ID: {0}", customerIdString);
        Long customerId = validateAndParseId(customerIdString, "Customer");
        Cart cart = cartRepository.getCartByCustomerId(customerId);
        LOGGER.log(Level.INFO, "REST - Retrieved cart with {0} items for customer ID: {1}", new Object[]{cart.getItems().size(), customerId});
        return cart;
    }

    @POST
    @Path("/items")
    public Cart addItemToCart(@PathParam("customerId") String customerIdString, CartItem item) {
        LOGGER.log(Level.INFO, "REST - Adding item to cart for customer ID: {0}", customerIdString);
        Long customerId = validateAndParseId(customerIdString, "Customer");
        
        if (item.getBookId() == null) {
            LOGGER.warning("REST - Add to cart failed: Book ID is empty");
            throw new InvalidInputException("Book ID cannot be empty.");
        }
        
        // Validate quantity
        if (item.getQuantity() == null) {
            throw new InvalidInputException("Quantity cannot be empty.");
        }
        
        LOGGER.log(Level.INFO, "REST - Adding book ID: {0} with quantity: {1}", new Object[]{item.getBookId(), item.getQuantity()});
        Cart cart = cartRepository.addOrCreateCart(customerId, item);
        LOGGER.info("REST - Item added to cart successfully");
        return cart;
    }

    @PUT
    @Path("/items/{bookId}")
    public UpdateResponse<Cart> updateCartItem(
            @PathParam("customerId") String customerIdString,
            @PathParam("bookId") String bookIdString, CartItem item) {
        LOGGER.log(Level.INFO, "REST - Updating cart item for customer ID: {0}, book ID: {1}", new Object[]{customerIdString, bookIdString});
        Long customerId = validateAndParseId(customerIdString, "Customer");
        Long bookId = validateAndParseId(bookIdString, "Book");
        
        LOGGER.log(Level.INFO, "REST - Updating quantity to: {0}", item.getQuantity());
        UpdateResponse<Cart> response = cartRepository.updateCartItemQuantity(customerId, bookId, item);
        LOGGER.info("REST - Cart item updated successfully");
        return response;
    }

    @DELETE
    @Path("/items/{bookId}")
    public DeleteResponse removeCartItem(
            @PathParam("customerId") String customerIdString,
            @PathParam("bookId") String bookIdString) {
        LOGGER.log(Level.INFO, "REST - Removing cart item for customer ID: {0}, book ID: {1}", new Object[]{customerIdString, bookIdString});
        Long customerId = validateAndParseId(customerIdString, "Customer");
        Long bookId = validateAndParseId(bookIdString, "Book");
        
        DeleteResponse response = cartRepository.removeCartItem(customerId, bookId);
        LOGGER.info("REST - Cart item removed successfully");
        return response;
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