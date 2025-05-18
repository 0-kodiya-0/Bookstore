package com.example.bookstore.bean;

import com.example.bookstore.models.Customer;
import com.example.bookstore.models.LoginRequest;
import com.example.bookstore.models.LoginResponse;
import com.example.bookstore.resources.AuthResource;
import com.example.bookstore.utils.JwtUtil;
import com.example.bookstore.utils.RestClient;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@ManagedBean
@SessionScoped
public class CustomerBean implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private static final Logger LOGGER = Logger.getLogger(AuthResource.class.getName());

    private Customer customer = new Customer();
    private Customer loggedInCustomer;
    private String confirmPassword;
    private String jwtToken;
    private RestClient restClient = new RestClient();

    @PostConstruct
    public void init() {
        // Check for existing JWT cookie or session first
        checkExistingAuthentication();

        // Check if there's a customer ID in the request parameter
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap();
        String customerId = params.get("id");
        if (customerId != null && !customerId.isEmpty()) {
            try {
                Long id = Long.parseLong(customerId);
                loadCustomer(id);
            } catch (NumberFormatException e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Invalid customer ID"));
            }
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

    // Update the register method in CustomerBean.java
    public String register() {
        // Validate password confirmation
        if (!customer.getPassword().equals(confirmPassword)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Passwords do not match"));
            return null;
        }

        try {
            // Use the auth/register endpoint with explicit LoginResponse type
            LoginResponse response = restClient.post("auth/register", customer, LoginResponse.class);

            if (response == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Registration failed"));
                return null;
            }

            // Store logged in customer and JWT token
            loggedInCustomer = response.getCustomer();
            jwtToken = response.getToken();

            // Set JWT token in the RestClient
            restClient.setJwtToken(jwtToken);

            // Store JWT in cookie
            HttpServletResponse httpResponse = (HttpServletResponse) FacesContext.getCurrentInstance()
                    .getExternalContext().getResponse();

            Cookie tokenCookie = new Cookie("jwt_token", jwtToken);
            tokenCookie.setMaxAge(24 * 60 * 60); // 24 hours expiry
            tokenCookie.setPath("/");
            tokenCookie.setHttpOnly(true); // For security
            httpResponse.addCookie(tokenCookie);

            // Store in session
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
                    .getExternalContext().getSession(true);
            session.setAttribute("loggedInCustomer", loggedInCustomer);
            session.setAttribute("jwtToken", jwtToken);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Registration successful"));

            return "/index?faces-redirect=true";
        } catch (Exception e) {
            // Detailed logging for debugging
            LOGGER.log(Level.SEVERE, "Registration error: " + e.getMessage(), e);

            // Check for specific error messages
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("email already exists")) {
                // Provide more user-friendly error message for duplicate email
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registration Error",
                                "An account with this email already exists. Please try logging in or use a different email."));
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registration Error", errorMsg));
            }
            return null;
        }
    }

    public String login() {
        try {
            // Create login request
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail(customer.getEmail());
            loginRequest.setPassword(customer.getPassword());

            // Call authentication API
            LoginResponse response = restClient.post("auth/login", loginRequest, LoginResponse.class);

            if (response == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Authentication failed"));
                return null;
            }

            // Store logged in customer and JWT token
            loggedInCustomer = response.getCustomer();
            jwtToken = response.getToken();

            // Set JWT token in the RestClient for subsequent API calls
            restClient.setJwtToken(jwtToken);

            // Store JWT in cookie
            HttpServletResponse httpResponse = (HttpServletResponse) FacesContext.getCurrentInstance()
                    .getExternalContext().getResponse();

            Cookie tokenCookie = new Cookie("jwt_token", jwtToken);
            tokenCookie.setMaxAge(24 * 60 * 60); // 24 hours expiry
            tokenCookie.setPath("/");
            tokenCookie.setHttpOnly(true); // For security
            httpResponse.addCookie(tokenCookie);

            // Store in session
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
                    .getExternalContext().getSession(true);
            session.setAttribute("loggedInCustomer", loggedInCustomer);
            session.setAttribute("jwtToken", jwtToken);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Login successful"));

            return "/index?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
            return null;
        }
    }

    public String logout() {
        // Clear stored data
        loggedInCustomer = null;
        jwtToken = null;

        // Clear JWT from RestClient
        restClient.setJwtToken(null);

        // Clear JWT cookie
        HttpServletResponse httpResponse = (HttpServletResponse) FacesContext.getCurrentInstance()
                .getExternalContext().getResponse();

        Cookie tokenCookie = new Cookie("jwt_token", "");
        tokenCookie.setMaxAge(0); // Expire immediately
        tokenCookie.setPath("/");
        httpResponse.addCookie(tokenCookie);

        // Invalidate session
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(false);
        if (session != null) {
            session.invalidate();
        }

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "You have been logged out"));

        return "/index?faces-redirect=true";
    }

    public void loadCustomer(Long id) {
        customer = restClient.get("customers/" + id, Customer.class);
    }

    public String updateProfile() {
        try {
            restClient.put("customers/" + loggedInCustomer.getId(), customer);

            // Update session with updated customer
            loggedInCustomer = customer;
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
                    .getExternalContext().getSession(false);
            session.setAttribute("loggedInCustomer", loggedInCustomer);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Profile updated successfully"));

            return "/customers/profile?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
            return null;
        }
    }

    public boolean isLoggedIn() {
        return loggedInCustomer != null;
    }

    // Getters and setters
    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Customer getLoggedInCustomer() {
        return loggedInCustomer;
    }

    public void setLoggedInCustomer(Customer loggedInCustomer) {
        this.loggedInCustomer = loggedInCustomer;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }
}
