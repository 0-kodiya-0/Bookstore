package com.example.bookstore.filters;

import com.example.bookstore.utils.JwtUtil;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthenticationFilter implements ContainerRequestFilter {
    
    private static final Logger LOGGER = Logger.getLogger(JwtAuthenticationFilter.class.getName());
    
    // Paths that don't require authentication
    private static final List<String> EXEMPT_PATHS = Arrays.asList(
            "auth",
            "books",
            "authors",
            "customers" // Allow customer registration
    );
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod();
        
        // Allow OPTIONS requests to pass through (for CORS)
        if ("OPTIONS".equals(method)) {
            return;
        }
        
        // Check if path is exempt from authentication
        for (String exemptPath : EXEMPT_PATHS) {
            if (path.startsWith(exemptPath)) {
                // For customers path, only allow POST (registration) and GET (public endpoint) without authentication
                if (exemptPath.equals("customers") && !("POST".equals(method) || "GET".equals(method))) {
                    break; // Need authentication for PUT/DELETE on customers
                }
                return; // Allow request to pass through
            }
        }
        
        // Check for auth/login specific endpoint
        if (path.equals("auth/login")) {
            return;
        }
        
        // Get the Authorization header
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        
        // Validate the Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            LOGGER.log(Level.WARNING, "Missing or invalid Authorization header for path: {0}", path);
            abortWithUnauthorized(requestContext);
            return;
        }
        
        // Extract the token
        String token = authHeader.substring("Bearer ".length()).trim();
        
        // Validate the token
        try {
            if (JwtUtil.validateTokenAndGetClaims(token) == null) {
                LOGGER.log(Level.WARNING, "Invalid token for path: {0}", path);
                abortWithUnauthorized(requestContext);
                return;
            }
            
            // Set customer ID in request property for use in resources
            Long customerId = JwtUtil.getCustomerIdFromToken(token);
            requestContext.setProperty("customerId", customerId);
            
            LOGGER.log(Level.FINE, "Authenticated request for customer ID: {0}, path: {1}", 
                    new Object[]{customerId, path});
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error validating token: {0}", e.getMessage());
            abortWithUnauthorized(requestContext);
        }
    }
    
    private void abortWithUnauthorized(ContainerRequestContext requestContext) {
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Unauthorized\",\"message\":\"Authentication required.\"}")
                        .type("application/json")
                        .build());
    }
}