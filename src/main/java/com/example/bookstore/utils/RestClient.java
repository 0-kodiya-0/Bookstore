package com.example.bookstore.utils;

import com.example.bookstore.models.UpdateResponse;
import java.io.Serializable;
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

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.util.Map;

public class RestClient implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(RestClient.class.getName());

    private static final String API_BASE_URL = "http://localhost:8080/bookstore/api/";
    private final Client client;
    private final Gson gson;

    // JWT token for authentication
    private String jwtToken;

    public RestClient() {
        this.client = ClientBuilder.newClient();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    @Override
                    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        try {
                            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                            parser.setTimeZone(TimeZone.getTimeZone("UTC"));
                            return parser.parse(json.getAsString());
                        } catch (ParseException e) {
                            return null;
                        }
                    }
                })
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();
    }

    // Setter for JWT token
    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    // Getter for JWT token
    public String getJwtToken() {
        return jwtToken;
    }

    public <T> T get(String path, Class<T> responseType) {
        try {
            WebTarget target = client.target(API_BASE_URL + path);
            Response response;

            if (jwtToken != null && !jwtToken.isEmpty()) {
                // Send request with Authorization header
                response = target
                        .request(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .get();
            } else {
                // Send request without Authorization header
                response = target
                        .request(MediaType.APPLICATION_JSON)
                        .get();
            }

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
            Response response;

            if (jwtToken != null && !jwtToken.isEmpty()) {
                // Send request with Authorization header
                response = target
                        .request(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .get();
            } else {
                // Send request without Authorization header
                response = target
                        .request(MediaType.APPLICATION_JSON)
                        .get();
            }

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

    public <T> T post(String path, Object requestEntity, Class<T> responseType) {
        try {
            WebTarget target = client.target(API_BASE_URL + path);
            Response response;

            if (jwtToken != null && !jwtToken.isEmpty()) {
                response = target
                        .request(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .post(Entity.entity(requestEntity, MediaType.APPLICATION_JSON));
            } else {
                response = target
                        .request(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(requestEntity, MediaType.APPLICATION_JSON));
            }

            if (response.getStatus() == Response.Status.CREATED.getStatusCode()
                    || response.getStatus() == Response.Status.OK.getStatusCode()) {

                // Use the specific class type for deserialization
                if (responseType != null) {
                    String jsonStr = response.readEntity(String.class);
                    return gson.fromJson(jsonStr, responseType);
                } else {
                    return (T) response.readEntity(Object.class);
                }
            } else {
                handleErrorResponse(response);
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in POST request: " + path, e);
            throw new RuntimeException("Error accessing the API: " + e.getMessage());
        }
    }

    // Add a simplified version for backward compatibility
    public <T> T post(String path, Object requestEntity) {
        return post(path, requestEntity, null);
    }

    public <T> UpdateResponse<T> put(String path, Object requestEntity, Class<T> entityType) {
        try {
            WebTarget target = client.target(API_BASE_URL + path);
            Response response;

            if (jwtToken != null && !jwtToken.isEmpty()) {
                response = target
                        .request(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .put(Entity.entity(requestEntity, MediaType.APPLICATION_JSON));
            } else {
                response = target
                        .request(MediaType.APPLICATION_JSON)
                        .put(Entity.entity(requestEntity, MediaType.APPLICATION_JSON));
            }

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                String jsonStr = response.readEntity(String.class);

                // First deserialize to a Map to access the structure
                Map<String, Object> responseMap = gson.fromJson(jsonStr, Map.class);

                // Create a new UpdateResponse
                UpdateResponse<T> result = new UpdateResponse<>();

                // Set basic properties
                if (responseMap.containsKey("updated")) {
                    result.setUpdated((Boolean) responseMap.get("updated"));
                }

                if (responseMap.containsKey("fieldsUpdated")) {
                    // Handle potential double coming from JSON
                    Object fieldsObj = responseMap.get("fieldsUpdated");
                    if (fieldsObj instanceof Number) {
                        result.setFieldsUpdated(((Number) fieldsObj).intValue());
                    }
                }

                // Properly handle the entity conversion
                if (entityType != null && responseMap.containsKey("entity")) {
                    // Convert just the entity portion to the proper type
                    String entityJson = gson.toJson(responseMap.get("entity"));
                    T entity = gson.fromJson(entityJson, entityType);
                    result.setEntity(entity);
                }

                return result;
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
            Response response;

            if (jwtToken != null && !jwtToken.isEmpty()) {
                // Send request with Authorization header
                response = target
                        .request(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .delete();
            } else {
                // Send request without Authorization header
                response = target
                        .request(MediaType.APPLICATION_JSON)
                        .delete();
            }

            if (response.getStatus() != Response.Status.OK.getStatusCode()
                    && response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
                handleErrorResponse(response);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in DELETE request: " + path, e);
            throw new RuntimeException("Error accessing the API: " + e.getMessage());
        }
    }

    public <T> List<T> search(String path, String searchTerm, Class<T> elementType) {
        try {
            WebTarget target = client.target(API_BASE_URL + path);

            // Add query parameter if search term is provided
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                target = target.queryParam("query", searchTerm);
            }

            Response response;

            if (jwtToken != null && !jwtToken.isEmpty()) {
                // Send request with Authorization header
                response = target
                        .request(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .get();
            } else {
                // Send request without Authorization header
                response = target
                        .request(MediaType.APPLICATION_JSON)
                        .get();
            }

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                String json = response.readEntity(String.class);
                Type listType = TypeToken.getParameterized(ArrayList.class, elementType).getType();
                return gson.fromJson(json, listType);
            } else {
                handleErrorResponse(response);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in SEARCH request: " + path, e);
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
            throw new RuntimeException("Error processing API response: " + e.getMessage()
                    + ". HTTP Status: " + response.getStatus());
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
