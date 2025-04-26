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

@Path("/authors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorBaseResource {

    private final AuthorRepository authorRepository = AppConfig.getAuthorRepository();

    @POST
    public Response createAuthor(Author author, @Context UriInfo uriInfo) {
        if (author.getName() == null) {
            throw new InvalidInputException("Author name cannot be empty.");
        }

        // AuthorRepository will check for duplicate names internally
        Author createdAuthor = authorRepository.addAuthor(author);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(createdAuthor.getId())).build();

        return Response.created(location).entity(createdAuthor).build();
    }

    @GET
    public List<Author> getAllAuthors() {
        return authorRepository.getAllAuthors();
    }
}