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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ManagedBean
@ViewScoped
public class OrderBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(OrderBean.class.getName());

    private List<Order> orders = new ArrayList<>();
    private Order order = new Order();
    private Customer loggedInCustomer;
    private String jwtToken;
    private RestClient restClient = new RestClient();

    // Date formats for parsing and displaying
    private SimpleDateFormat isoFormat;
    private SimpleDateFormat displayFormat;

    // Your local timezone - change this to your local timezone
    private String localTimeZone = "Asia/Colombo"; // Change this to your timezone (example: Asia/Colombo for Sri Lanka)

    @PostConstruct
    public void init() {
        isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        displayFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
        displayFormat.setTimeZone(TimeZone.getTimeZone(localTimeZone));

        // Initialize JWT token from session if available
        checkExistingAuthentication();

        if (loggedInCustomer != null) {
            try {
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
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error loading orders: " + e.getMessage(), e);
                // Initialize empty list on error
                orders = new ArrayList<>();

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not load orders. Please try again later."));
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
            if (orders == null) {
                orders = new ArrayList<>(); // Ensure we never have null orders
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in loadCustomerOrders: " + e.getMessage(), e);
            // Initialize empty list on error
            orders = new ArrayList<>();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to load orders: " + e.getMessage()));
        }
    }

    public void loadOrder(Long customerId, Long orderId) {
        try {
            order = restClient.get("customers/" + customerId + "/orders/" + orderId, Order.class);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in loadOrder: " + e.getMessage(), e);
            // Initialize empty order on error
            order = new Order();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to load order: " + e.getMessage()));
        }
    }

    /**
     * Format the date for display in the UI with timezone conversion Can handle
     * both java.util.Date and String representations
     */
    public String formatOrderDate(Object dateObj) {
        try {
            if (dateObj == null) {
                return "";
            }

            Date date;
            if (dateObj instanceof Date) {
                date = (Date) dateObj;
            } else if (dateObj instanceof String) {
                try {
                    // Try to parse the date string
                    date = isoFormat.parse((String) dateObj);
                } catch (ParseException e) {
                    LOGGER.log(Level.WARNING, "Could not parse date: " + dateObj);
                    return dateObj.toString();
                }
            } else {
                return dateObj.toString();
            }

            return displayFormat.format(date);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error formatting date: " + e.getMessage());
            return String.valueOf(dateObj);
        }
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

    public String getLocalTimeZone() {
        return localTimeZone;
    }
}
