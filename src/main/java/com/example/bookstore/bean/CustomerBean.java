package com.example.bookstore.bean;

import com.example.bookstore.models.Customer;
import com.example.bookstore.utils.RestClient;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.Map;

@ManagedBean
@SessionScoped
public class CustomerBean implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Customer customer = new Customer();
    private Customer loggedInCustomer;
    private String confirmPassword;
    private RestClient restClient = new RestClient();
    
    @PostConstruct
    public void init() {
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
    
    public String register() {
        // Validate password confirmation
        if (!customer.getPassword().equals(confirmPassword)) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Passwords do not match"));
            return null;
        }
        
        try {
            Customer registeredCustomer = restClient.post("customers", customer);
            
            // Auto login after registration
            login();
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Registration successful"));
            
            return "/index?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
            return null;
        }
    }
    
    public String login() {
        try {
            // In a real application, you would have an authentication API endpoint
            // For this demo, we'll just fetch the customer by email and validate the password manually
            
            // For demo purposes only - in a real app you'd use a proper auth endpoint
            // This is just simulating authentication against the customer repository
            Customer foundCustomer = null;
            
            // In a real app, this would be a single API call, not iterating through customers
            for (Customer c : restClient.getAll("customers", Customer.class)) {
                if (c.getEmail().equals(customer.getEmail())) {
                    foundCustomer = c;
                    break;
                }
            }
            
            if (foundCustomer != null && foundCustomer.getPassword().equals(customer.getPassword())) {
                // Store logged in customer in session
                loggedInCustomer = foundCustomer;
                HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
                                    .getExternalContext().getSession(true);
                session.setAttribute("loggedInCustomer", loggedInCustomer);
                
                return "/index?faces-redirect=true";
            } else {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Invalid email or password"));
                return null;
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
            return null;
        }
    }
    
    public String logout() {
        loggedInCustomer = null;
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
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
}
