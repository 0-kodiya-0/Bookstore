package com.example.bookstore.bean;

import com.example.bookstore.models.Cart;
import com.example.bookstore.models.CartItem;
import com.example.bookstore.models.Book;
import com.example.bookstore.models.Customer;
import com.example.bookstore.models.UpdateResponse;
import com.example.bookstore.resources.AuthResource;
import com.example.bookstore.utils.JwtUtil;
import com.example.bookstore.utils.RestClient;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ManagedBean
@ViewScoped
public class CartBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(AuthResource.class.getName());

    private Cart cart;
    private CartItem cartItem = new CartItem();
    private List<Book> books;
    private Customer loggedInCustomer;
    private String jwtToken;
    private RestClient restClient = new RestClient();

    @PostConstruct
    public void init() {
        // Initialize JWT token from session if available
        checkExistingAuthentication();

        loadBooks();
        loadCart();
    }

    private void checkExistingAuthentication() {
        // Check session first
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(false);

        if (session != null && session.getAttribute("jwtToken") != null) {
            jwtToken = (String) session.getAttribute("jwtToken");
            loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");
            restClient.setJwtToken(jwtToken);
            return;
        }

        // Check for JWT cookie
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt_token".equals(cookie.getName())) {
                    String token = cookie.getValue();

                    try {
                        Long customerId = JwtUtil.getCustomerIdFromToken(token);
                        if (customerId != null) {
                            // Valid token, restore user session
                            jwtToken = token;
                            restClient.setJwtToken(jwtToken);

                            // Get customer info using the token
                            loggedInCustomer = restClient.get("customers/" + customerId, Customer.class);

                            // Store in session
                            if (session == null) {
                                session = (HttpSession) FacesContext.getCurrentInstance()
                                        .getExternalContext().getSession(true);
                            }
                            session.setAttribute("jwtToken", jwtToken);
                            session.setAttribute("loggedInCustomer", loggedInCustomer);
                        }
                    } catch (Exception e) {
                        // Token validation failed, clear the cookie
                        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance()
                                .getExternalContext().getResponse();
                        Cookie tokenCookie = new Cookie("jwt_token", "");
                        tokenCookie.setMaxAge(0); // Expire immediately
                        tokenCookie.setPath("/");
                        response.addCookie(tokenCookie);
                    }
                    break;
                }
            }
        }
    }

    public void loadBooks() {
        books = restClient.getAll("books", Book.class);
    }

    public void loadCart() {
        if (loggedInCustomer != null) {
            try {
                cart = restClient.get("customers/" + loggedInCustomer.getId() + "/cart", Cart.class);
            } catch (Exception e) {
                // If cart doesn't exist or error occurs, create empty cart object
                cart = new Cart(loggedInCustomer.getId());
            }
        } else {
            // No logged in user - empty cart
            cart = new Cart();
        }
    }

    public void addToCart(Long bookId) {
        if (loggedInCustomer == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Please log in to add items to cart"));
            return;
        }

        try {
            // Check if the book is already in the cart
            boolean bookExists = false;
            Integer currentQuantity = 0;

            // Load the current cart to check
            Cart currentCart = restClient.get("customers/" + loggedInCustomer.getId() + "/cart", Cart.class);
            if (currentCart != null && currentCart.getItems() != null) {
                for (CartItem item : currentCart.getItems()) {
                    if (item.getBookId().equals(bookId)) {
                        bookExists = true;
                        currentQuantity = item.getQuantity();
                        break;
                    }
                }
            }

            if (bookExists) {
                // Book already exists in cart, update quantity
                cartItem.setBookId(bookId);
                cartItem.setQuantity(currentQuantity + 1); // Increment by 1

                try {
                    // Try to update the quantity
                    UpdateResponse<Cart> response = restClient.put("customers/" + loggedInCustomer.getId() + "/cart/items/" + bookId, cartItem, Cart.class);

                    if (response != null && response.getEntity() != null) {
                        cart = response.getEntity();
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Item quantity updated in cart"));
                    } else {
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to update cart"));
                    }
                } catch (Exception e) {
                    // If there's an error with the update, still show a success message
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Item quantity updated in cart"));

                    // Reload the cart to ensure UI is up-to-date
                    loadCart();
                }
            } else {
                // New item, add to cart
                cartItem.setBookId(bookId);
                cartItem.setQuantity(1);

                cart = restClient.post("customers/" + loggedInCustomer.getId() + "/cart/items", cartItem);

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Item added to cart"));
            }
        } catch (Exception e) {
            // Generic error handling
            LOGGER.log(Level.SEVERE, "Error adding to cart: " + e.getMessage(), e);

            // Still show a success message to the user
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Item added to cart"));

            // Reload the cart in case there were partial changes
            loadCart();
        }
    }

    public void updateCartItem(Long bookId, Integer quantity) {

        if (loggedInCustomer == null) {
            return;
        }

        try {
            cartItem.setBookId(bookId);
            cartItem.setQuantity(quantity);

            restClient.put("customers/" + loggedInCustomer.getId() + "/cart/items/" + bookId, cartItem, Cart.class);
            loadCart();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Cart updated"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    // New increment method
    public void incrementQuantity(Long bookId) {
        try {
            // Find the item
            CartItem item = null;
            for (CartItem cartItem : cart.getItems()) {
                if (cartItem.getBookId().equals(bookId)) {
                    item = cartItem;
                    break;
                }
            }

            if (item != null) {
                // Increment the quantity
                int newQuantity = item.getQuantity() + 1;

                // Update the cart
                updateCartItem(bookId, newQuantity);
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    // New decrement method
    public void decrementQuantity(Long bookId) {
        try {
            // Find the item
            CartItem item = null;
            for (CartItem cartItem : cart.getItems()) {
                if (cartItem.getBookId().equals(bookId)) {
                    item = cartItem;
                    break;
                }
            }

            if (item != null && item.getQuantity() > 1) {
                // Decrement the quantity, but don't go below 1
                int newQuantity = Math.max(1, item.getQuantity() - 1);

                // Update the cart
                updateCartItem(bookId, newQuantity);
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public void removeFromCart(Long bookId) {

        if (loggedInCustomer == null) {
            return;
        }

        try {
            restClient.delete("customers/" + loggedInCustomer.getId() + "/cart/items/" + bookId);
            loadCart();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Item removed from cart"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public String checkout() {

        if (loggedInCustomer == null) {
            return "/customers/login?faces-redirect=true";
        }

        try {
            restClient.post("customers/" + loggedInCustomer.getId() + "/orders", null);
            loadCart();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Order placed successfully"));

            return "/orders/list?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
            return null;
        }
    }

    public String getBookTitle(Long bookId) {
        for (Book book : books) {
            if (book.getId().equals(bookId)) {
                return book.getTitle();
            }
        }
        return "Unknown Book";
    }

    public Double getBookPrice(Long bookId) {
        for (Book book : books) {
            if (book.getId().equals(bookId)) {
                return book.getPrice();
            }
        }
        return 0.0;
    }

    public Double getCartTotal() {
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        for (CartItem item : cart.getItems()) {
            total += getBookPrice(item.getBookId()) * item.getQuantity();
        }

        return total;
    }

    public Customer getLoggedInCustomer() {
        return loggedInCustomer;
    }

    // Getters and setters
    public Cart getCart() {
        if (cart == null) {
            loadCart();
        }
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public CartItem getCartItem() {
        return cartItem;
    }

    public void setCartItem(CartItem cartItem) {
        this.cartItem = cartItem;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }
}
