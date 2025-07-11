package com.example.bookstore.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "customers")
public class Customer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(name = "password", nullable = false, length = 255)
    @JsonIgnore // Never serialize password in JSON
    private String password;
    
    // One customer can have many orders
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    private List<Order> orders = new ArrayList<>();
    
    // One customer can have many cart items
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    private List<CartItem> cartItems = new ArrayList<>();

    // Default constructor required by JPA
    public Customer() {
    }

    public Customer(Customer customer) {
        this.id = customer.id;
        this.name = customer.name;
        this.email = customer.email;
        this.password = customer.password;
    }

    public Customer(Long id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public List<Order> getOrders() {
        return orders;
    }
    
    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
    
    public List<CartItem> getCartItems() {
        return cartItems;
    }
    
    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }
    
    // Helper method to add an order
    public void addOrder(Order order) {
        orders.add(order);
        order.setCustomer(this);
    }
    
    // Helper method to remove an order
    public void removeOrder(Order order) {
        orders.remove(order);
        order.setCustomer(null);
    }
    
    // Helper method to add a cart item
    public void addCartItem(CartItem cartItem) {
        cartItems.add(cartItem);
        cartItem.setCustomer(this);
    }
    
    // Helper method to remove a cart item
    public void removeCartItem(CartItem cartItem) {
        cartItems.remove(cartItem);
        cartItem.setCustomer(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        Customer customer = (Customer) o;
        return id != null && id.equals(customer.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Customer{" + 
                "id=" + id + 
                ", name='" + name + '\'' + 
                ", email='" + email + '\'' + 
                ", password='" + password + '\'' + 
                '}';
    }
}