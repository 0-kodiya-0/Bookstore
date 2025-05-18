package com.example.bookstore.utils;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class RestClient implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(RestClient.class.getName());
    
    private static final String API_BASE_URL = "http://localhost:8080/bookstore/api/";
    private final Client client;
    private final Gson gson;
    
    public RestClient() {
        this.client = ClientBuilder.newClient();
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();
    }
    
    public <T> T get(String path, Class<T> responseType) {
        try {
            WebTarget target = client.target(API_BASE_URL + path);
            Response response = target
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                return response.readEntity(responseType);
            } else {
                handleErrorResponse(response);
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in GET request: " + path, e);
            throw new RuntimeException("Error accessing the API: " + e.getMessage());
        }
    }
    
    public <T> List<T> getAll(String path, Class<T> elementType) {
        try {
            WebTarget target = client.target(API_BASE_URL + path);
            Response response = target
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                String json = response.readEntity(String.class);
                Type listType = TypeToken.getParameterized(ArrayList.class, elementType).getType();
                return gson.fromJson(json, listType);
            } else {
                handleErrorResponse(response);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in GET ALL request: " + path, e);
            throw new RuntimeException("Error accessing the API: " + e.getMessage());
        }
    }
    
    public <T> T post(String path, Object requestEntity) {
        try {
            WebTarget target = client.target(API_BASE_URL + path);
            Response response = target
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(requestEntity, MediaType.APPLICATION_JSON));
            
            if (response.getStatus() == Response.Status.CREATED.getStatusCode() ||
                response.getStatus() == Response.Status.OK.getStatusCode()) {
                return (T) response.readEntity(Object.class);
            } else {
                handleErrorResponse(response);
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in POST request: " + path, e);
            throw new RuntimeException("Error accessing the API: " + e.getMessage());
        }
    }
    
    public <T> T put(String path, Object requestEntity) {
        try {
            WebTarget target = client.target(API_BASE_URL + path);
            Response response = target
                    .request(MediaType.APPLICATION_JSON)
                    .put(Entity.entity(requestEntity, MediaType.APPLICATION_JSON));
            
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                return (T) response.readEntity(Object.class);
            } else {
                handleErrorResponse(response);
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in PUT request: " + path, e);
            throw new RuntimeException("Error accessing the API: " + e.getMessage());
        }
    }
    
    public void delete(String path) {
        try {
            WebTarget target = client.target(API_BASE_URL + path);
            Response response = target
                    .request(MediaType.APPLICATION_JSON)
                    .delete();
            
            if (response.getStatus() != Response.Status.OK.getStatusCode() &&
                response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
                handleErrorResponse(response);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in DELETE request: " + path, e);
            throw new RuntimeException("Error accessing the API: " + e.getMessage());
        }
    }
    
    private void handleErrorResponse(Response response) {
        try {
            String errorJson = response.readEntity(String.class);
            LOGGER.log(Level.WARNING, "API Error Response: " + errorJson);
            
            ErrorResponse errorResponse = gson.fromJson(errorJson, ErrorResponse.class);
            throw new RuntimeException(errorResponse.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error processing API response: " + e.getMessage() + 
                    ". HTTP Status: " + response.getStatus());
        }
    }
    
    // Simple error response class matching your API's error format
    private static class ErrorResponse {
        private String error;
        private String message;
        
        public String getError() {
            return error;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
