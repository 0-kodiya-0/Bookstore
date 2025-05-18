package com.example.bookstore.bean;

import com.example.bookstore.models.Cart;
import com.example.bookstore.models.CartItem;
import com.example.bookstore.models.Book;
import com.example.bookstore.models.Customer;
import com.example.bookstore.resources.AuthResource;
import com.example.bookstore.utils.JwtUtil;
import com.example.bookstore.utils.RestClient;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ViewScoped;
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
        
        LOGGER.log(Level.INFO, loggedInCustomer.toString());
        
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
        LOGGER.log(Level.INFO, books.toString());
    }
    
    public void loadCart() {
        if (loggedInCustomer != null) {
            try {
                cart = restClient.get("customers/" + loggedInCustomer.getId() + "/cart", Cart.class);
                LOGGER.log(Level.INFO, cart.toString());
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
            cartItem.setBookId(bookId);
            cartItem.setQuantity(1);
            
            cart = restClient.post("customers/" + loggedInCustomer.getId() + "/cart/items", cartItem);
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Item added to cart"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }
    
    public void updateCartItem(Long bookId, Integer quantity) {
        
        if (loggedInCustomer == null) {
            return;
        }
        
        try {
            cartItem.setBookId(bookId);
            cartItem.setQuantity(quantity);
            
            restClient.put("customers/" + loggedInCustomer.getId() + "/cart/items/" + bookId, cartItem);
            loadCart();
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Cart updated"));
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