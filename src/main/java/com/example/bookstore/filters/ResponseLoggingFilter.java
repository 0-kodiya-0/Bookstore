package com.example.bookstore.filters;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class ResponseLoggingFilter implements ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(ResponseLoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext,
            ContainerResponseContext responseContext) throws IOException {
        LOGGER.log(Level.INFO, "\n===== HTTP Response =====\n    Status: {0}\n    Headers: {1}\n=========================",
                new Object[]{
                    responseContext.getStatus(),
                    responseContext.getHeaders()
                });
    }
}
