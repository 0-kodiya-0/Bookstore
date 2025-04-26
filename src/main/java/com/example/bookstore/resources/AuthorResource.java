package com.example.bookstore.resources;

import com.example.bookstore.config.AppConfig;
import com.example.bookstore.exception.InvalidInputException;
import com.example.bookstore.models.Author;
import com.example.bookstore.models.Book;
import com.example.bookstore.models.DeleteResponse;
import com.example.bookstore.models.UpdateResponse;
import com.example.bookstore.repository.AuthorRepository;
import com.example.bookstore.repository.BookRepository;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

/**
 *
 * @author HP
 */
@Path("/authors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorResource {

    private AuthorRepository authorRepository = AppConfig.getAuthorRepository();
    private BookRepository bookRepository = AppConfig.getBookRepository();

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

    @GET
    @Path("/{id}")
    public Author getAuthorById(@PathParam("id") Long id) {
        return authorRepository.getAuthorById(id);
    }

    @PUT
    @Path("/{id}")
    public Response updateAuthor(@PathParam("id") Long id, Author author) {
        // Repository will check for duplicate names on update
        UpdateResponse<Author> updateResponse = authorRepository.updateAuthor(id, author);
        return Response.ok(updateResponse).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteAuthor(@PathParam("id") Long id) {
        DeleteResponse deleteResponse = authorRepository.deleteAuthor(id);
        return Response.ok(deleteResponse).build();
    }

    @GET
    @Path("/{id}/books")
    public List<Book> getAuthorBooks(@PathParam("id") Long id) {
        // Verify author exists
        authorRepository.getAuthorById(id);

        // Get books by author
        return bookRepository.getBooksByAuthorId(id);
    }
}
