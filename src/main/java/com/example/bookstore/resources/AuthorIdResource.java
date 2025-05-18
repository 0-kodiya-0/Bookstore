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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/authors/{id}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorIdResource {

    private static final Logger LOGGER = Logger.getLogger(AuthorIdResource.class.getName());
    private final AuthorRepository authorRepository = AppConfig.getAuthorRepository();
    private final BookRepository bookRepository = AppConfig.getBookRepository();

    @GET
    public Author getAuthorById(@PathParam("id") String idString) {
        LOGGER.log(Level.INFO, "REST - Getting author by ID: {0}", idString);
        Long id = validateAndParseId(idString);
        Author author = authorRepository.getAuthorById(id);
        LOGGER.log(Level.INFO, "REST - Retrieved author: {0}", author.getName());
        return author;
    }

    @PUT
    public Response updateAuthor(@PathParam("id") String idString, Author author) {
        LOGGER.log(Level.INFO, "REST - Updating author with ID: {0}", idString);
        Long id = validateAndParseId(idString);
        UpdateResponse<Author> updateResponse = authorRepository.updateAuthor(id, author);
        LOGGER.log(Level.INFO, "REST - Author updated successfully. Fields updated: {0}", updateResponse.getFieldsUpdated());
        return Response.ok(updateResponse).build();
    }

    @DELETE
    public Response deleteAuthor(@PathParam("id") String idString) {
        LOGGER.log(Level.INFO, "REST - Deleting author with ID: {0}", idString);
        Long id = validateAndParseId(idString);
        DeleteResponse deleteResponse = authorRepository.deleteAuthor(id);
        LOGGER.log(Level.INFO, "REST - Author deleted successfully with ID: {0}", id);
        return Response.ok(deleteResponse).build();
    }

    @GET
    @Path("/books")
    public List<Book> getAuthorBooks(@PathParam("id") String idString) {
        LOGGER.log(Level.INFO, "REST - Getting books for author ID: {0}", idString);
        Long id = validateAndParseId(idString);
        // Verify author exists
        authorRepository.getAuthorById(id);
        
        // Get books by author
        List<Book> books = bookRepository.getBooksByAuthorId(id);
        LOGGER.log(Level.INFO, "REST - Retrieved {0} books for author ID: {1}", new Object[]{books.size(), id});
        return books;
    }

    private Long validateAndParseId(String idString) {
        if (idString == null || idString.trim().isEmpty()) {
            LOGGER.warning("REST - Author ID is empty");
            throw new InvalidInputException("Author ID cannot be empty.");
        }
        
        try {
            Long id = Long.valueOf(idString.trim());
            LOGGER.log(Level.FINE, "REST - Parsed author ID: {0}", id);
            return id;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "REST - Invalid author ID format: {0}", idString);
            throw new InvalidInputException("Author ID must be a valid number.");
        }
    }
}