package com.example.bookstore.bean;

import com.example.bookstore.models.Order;
import com.example.bookstore.models.Customer;
import com.example.bookstore.utils.RestClient;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@ManagedBean
@ViewScoped
public class OrderBean implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private List<Order> orders;
    private Order order;
    private RestClient restClient = new RestClient();
    
    @PostConstruct
    public void init() {
        Customer loggedInCustomer = getLoggedInCustomer();
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
        }
    }
    
    public void loadCustomerOrders(Long customerId) {
        try {
            orders = restClient.getAll("customers/" + customerId + "/orders", Order.class);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
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
    
    private Customer getLoggedInCustomer() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        return (session != null) ? (Customer) session.getAttribute("loggedInCustomer") : null;
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
