package com.example.bookstore.filters;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@PreMatching
public class TrailingSlashFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();

        // Remove trailing slash if present, except for root path
        if (path.length() > 1 && path.endsWith("/")) {
            UriBuilder uriBuilder = requestContext.getUriInfo().getRequestUriBuilder();
            uriBuilder.replacePath(path.substring(0, path.length() - 1));
            requestContext.setRequestUri(uriBuilder.build());
        }
    }
}
