package com.example.bookstore.resources;

import com.example.bookstore.config.AppConfig;
import com.example.bookstore.exception.InvalidInputException;
import com.example.bookstore.models.Author;
import com.example.bookstore.repository.AuthorRepository;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/authors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorBaseResource {

    private static final Logger LOGGER = Logger.getLogger(AuthorBaseResource.class.getName());
    private final AuthorRepository authorRepository = AppConfig.getAuthorRepository();

    @POST
    public Response createAuthor(Author author, @Context UriInfo uriInfo) {
        LOGGER.log(Level.INFO, "REST - Creating author: {0}", author.getName());
        
        if (author.getName() == null) {
            LOGGER.warning("REST - Create author failed: Name is empty");
            throw new InvalidInputException("Author name cannot be empty.");
        }

        // AuthorRepository will check for duplicate names internally
        Author createdAuthor = authorRepository.addAuthor(author);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(createdAuthor.getId())).build();

        LOGGER.log(Level.INFO, "REST - Author created successfully with ID: {0}", createdAuthor.getId());
        return Response.created(location).entity(createdAuthor).build();
    }

    @GET
    public List<Author> getAllAuthors() {
        LOGGER.info("REST - Getting all authors");
        List<Author> authors = authorRepository.getAllAuthors();
        LOGGER.log(Level.INFO, "REST - Retrieved {0} authors", authors.size());
        return authors;
    }
}