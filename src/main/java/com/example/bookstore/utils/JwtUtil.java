package com.example.bookstore.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;

public class JwtUtil {
    
    private static final Logger LOGGER = Logger.getLogger(JwtUtil.class.getName());
    
    // This should be in a secure configuration in a real application
    private static final String JWT_SECRET = "bookstore_secure_jwt_secret_key_for_authentication_and_authorization";
    
    // Token validity duration (24 hours)
    private static final long TOKEN_VALIDITY = 24 * 60 * 60 * 1000;
    
    // Get secret key from string
    private static SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    }
    
    // Generate JWT token
    public static String generateToken(Long customerId, String email, String name) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + TOKEN_VALIDITY);
        
        JwtBuilder builder = Jwts.builder()
                .setSubject(customerId.toString())
                .claim("email", email)
                .claim("name", name)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256);
        
        return builder.compact();
    }
    
    // Validate JWT token and extract claims
    public static Claims validateTokenAndGetClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Invalid JWT token: {0}", e.getMessage());
            return null;
        }
    }
    
    // Extract customer ID from token
    public static Long getCustomerIdFromToken(String token) {
        Claims claims = validateTokenAndGetClaims(token);
        if (claims != null) {
            return Long.parseLong(claims.getSubject());
        }
        return null;
    }
    
    // Extract email from token
    public static String getEmailFromToken(String token) {
        Claims claims = validateTokenAndGetClaims(token);
        if (claims != null) {
            return claims.get("email", String.class);
        }
        return null;
    }
    
    // Extract name from token
    public static String getNameFromToken(String token) {
        Claims claims = validateTokenAndGetClaims(token);
        if (claims != null) {
            return claims.get("name", String.class);
        }
        return null;
    }
}
