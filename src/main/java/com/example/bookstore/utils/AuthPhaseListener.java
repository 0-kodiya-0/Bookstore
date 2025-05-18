package com.example.bookstore.utils;

import com.example.bookstore.models.Customer;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class AuthPhaseListener implements PhaseListener, Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final List<String> PUBLIC_PAGES = Arrays.asList(
            "/index.xhtml",
            "/customers/login.xhtml",
            "/customers/register.xhtml",
            "/error.xhtml",
            "/books/list.xhtml",
            "/books/details.xhtml",
            "/authors/list.xhtml",
            "/authors/details.xhtml",
            "/resources"
    );

    @Override
    public void afterPhase(PhaseEvent event) {
        FacesContext facesContext = event.getFacesContext();
        String viewId = facesContext.getViewRoot().getViewId();
        
        // Allow public pages to be accessed without login
        for (String publicPage : PUBLIC_PAGES) {
            if (viewId.startsWith(publicPage)) {
                return;
            }
        }
        
        // Check if user is logged in via session
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
        if (session != null && session.getAttribute("loggedInCustomer") != null) {
            // User is logged in via session
            return;
        }
        
        // If not in session, check for JWT cookie
        HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        Cookie[] cookies = request.getCookies();
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt_token".equals(cookie.getName())) {
                    String jwtToken = cookie.getValue();
                    
                    // Validate token
                    try {
                        Long customerId = JwtUtil.getCustomerIdFromToken(jwtToken);
                        
                        if (customerId != null) {
                            // Valid token - restore the session
                            RestClient restClient = new RestClient();
                            restClient.setJwtToken(jwtToken);
                            
                            // Get customer details
                            Customer customer = restClient.get("customers/" + customerId, Customer.class);
                            
                            if (customer != null) {
                                // Create new session
                                session = (HttpSession) facesContext.getExternalContext().getSession(true);
                                session.setAttribute("loggedInCustomer", customer);
                                session.setAttribute("jwtToken", jwtToken);
                                
                                // Continue with the request - user is authenticated
                                return;
                            }
                        }
                    } catch (Exception e) {
                        // Invalid token - clear the cookie
                        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
                        Cookie tokenCookie = new Cookie("jwt_token", "");
                        tokenCookie.setMaxAge(0); // Expire immediately
                        tokenCookie.setPath("/");
                        response.addCookie(tokenCookie);
                    }
                }
            }
        }
        
        // Not authenticated - redirect to login
        try {
            facesContext.getExternalContext()
                .redirect(((HttpServletRequest) facesContext.getExternalContext()
                .getRequest()).getContextPath() + "/customers/login.xhtml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        // Not needed for authentication check
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }
}