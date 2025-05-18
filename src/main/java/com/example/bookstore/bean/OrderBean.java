package com.example.bookstore.bean;

import com.example.bookstore.models.Order;
import com.example.bookstore.models.Customer;
import com.example.bookstore.utils.JwtUtil;
import com.example.bookstore.utils.RestClient;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ManagedBean
@ViewScoped
public class OrderBean implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private List<Order> orders;
    private Order order;
    private Customer loggedInCustomer;
    private String jwtToken;
    private RestClient restClient = new RestClient();
    
    @PostConstruct
    public void init() {
        // Initialize JWT token from session if available
        checkExistingAuthentication();
        
        if (loggedInCustomer != null) {
            loadCustomerOrders(loggedInCustomer.getId());
            
            // Check if there's an order ID in the request parameter
            Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext()
                                        .getRequestParameterMap();
            String orderId = params.get("id");
            if (orderId != null && !orderId.isEmpty()) {
                try {
                    Long id = Long.parseLong(orderId);
                    loadOrder(loggedInCustomer.getId(), id);
                } catch (NumberFormatException e) {
                    FacesContext.getCurrentInstance().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Invalid order ID"));
                }
            }
        } else {
            // No logged in user, initialize empty lists
            orders = new ArrayList<>();
        }
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
    
    public void loadCustomerOrders(Long customerId) {
        try {
            orders = restClient.getAll("customers/" + customerId + "/orders", Order.class);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
            // Initialize empty list on error
            orders = new ArrayList<>();
        }
    }
    
    public void loadOrder(Long customerId, Long orderId) {
        try {
            order = restClient.get("customers/" + customerId + "/orders/" + orderId, Order.class);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public String formatOrderDate(java.time.LocalDateTime date) {
        if (date == null) {
            return "";
        }
        return date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }
    
    // Getters and setters
    public List<Order> getOrders() {
        return orders;
    }
    
    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
    
    public Order getOrder() {
        return order;
    }
    
    public void setOrder(Order order) {
        this.order = order;
    }
}