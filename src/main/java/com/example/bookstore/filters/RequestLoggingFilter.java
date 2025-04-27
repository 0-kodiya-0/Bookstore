package com.example.bookstore.filters;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class RequestLoggingFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(RequestLoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.log(Level.INFO, "\n===== HTTP Request ======\n    Method: {0}\n    Uri: {1}\n    User-Agent: {2}\n=========================",
                new Object[]{
                    requestContext.getMethod(),
                    requestContext.getUriInfo().getPath(),
                    requestContext.getHeaderString("User-Agent")
                });
    }
}
