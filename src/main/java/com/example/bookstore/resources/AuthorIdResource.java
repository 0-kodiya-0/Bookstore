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

@Path("/authors/{id}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorIdResource {

    private final AuthorRepository authorRepository = AppConfig.getAuthorRepository();
    private final BookRepository bookRepository = AppConfig.getBookRepository();

    @GET
    public Author getAuthorById(@PathParam("id") String idString) {
        Long id = validateAndParseId(idString);
        return authorRepository.getAuthorById(id);
    }

    @PUT
    public Response updateAuthor(@PathParam("id") String idString, Author author) {
        Long id = validateAndParseId(idString);
        UpdateResponse<Author> updateResponse = authorRepository.updateAuthor(id, author);
        return Response.ok(updateResponse).build();
    }

    @DELETE
    public Response deleteAuthor(@PathParam("id") String idString) {
        Long id = validateAndParseId(idString);
        DeleteResponse deleteResponse = authorRepository.deleteAuthor(id);
        return Response.ok(deleteResponse).build();
    }

    @GET
    @Path("/books")
    public List<Book> getAuthorBooks(@PathParam("id") String idString) {
        Long id = validateAndParseId(idString);
        // Verify author exists
        authorRepository.getAuthorById(id);
        
        // Get books by author
        return bookRepository.getBooksByAuthorId(id);
    }

    private Long validateAndParseId(String idString) {
        // For /authors/, idString will be empty string
        if (idString == null || idString.trim().isEmpty()) {
            throw new InvalidInputException("Author ID cannot be empty.");
        }
        
        try {
            return Long.parseLong(idString.trim());
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Author ID must be a valid number.");
        }
    }
}