package com.example.bookstore.bean;

import com.example.bookstore.models.Cart;
import com.example.bookstore.models.CartItem;
import com.example.bookstore.models.Book;
import com.example.bookstore.models.Customer;
import com.example.bookstore.utils.RestClient;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.List;

@ManagedBean
@SessionScoped
public class CartBean implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Cart cart;
    private CartItem cartItem = new CartItem();
    private List<Book> books;
    private RestClient restClient = new RestClient();
    
    @PostConstruct
    public void init() {
        // Initialize JWT token from session if available
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        if (session != null && session.getAttribute("jwtToken") != null) {
            String jwtToken = (String) session.getAttribute("jwtToken");
            restClient.setJwtToken(jwtToken);
        }
        
        loadBooks();
        loadCart();
    }
    
    public void loadBooks() {
        books = restClient.getAll("books", Book.class);
    }
    
    public void loadCart() {
        Customer loggedInCustomer = getLoggedInCustomer();
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
        Customer loggedInCustomer = getLoggedInCustomer();
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
        Customer loggedInCustomer = getLoggedInCustomer();
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
        Customer loggedInCustomer = getLoggedInCustomer();
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
        Customer loggedInCustomer = getLoggedInCustomer();
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
    
    private Customer getLoggedInCustomer() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        return (session != null) ? (Customer) session.getAttribute("loggedInCustomer") : null;
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