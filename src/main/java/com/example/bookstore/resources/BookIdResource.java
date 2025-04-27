package com.example.bookstore.resources;

import com.example.bookstore.config.AppConfig;
import com.example.bookstore.exception.InvalidInputException;
import com.example.bookstore.models.Book;
import com.example.bookstore.models.DeleteResponse;
import com.example.bookstore.models.UpdateResponse;
import com.example.bookstore.repository.BookRepository;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/books/{id}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookIdResource {

    private static final Logger LOGGER = Logger.getLogger(BookIdResource.class.getName());
    private BookRepository bookRepository = AppConfig.getBookRepository();

    @GET
    public Book getBookById(@PathParam("id") String idString) {
        LOGGER.log(Level.INFO, "REST - Getting book by ID: {0}", idString);
        Long id = validateAndParseId(idString);
        Book book = bookRepository.getBookById(id);
        LOGGER.log(Level.INFO, "REST - Retrieved book: {0}", book.getTitle());
        return book;
    }

    @PUT
    public Response updateBook(@PathParam("id") String idString, Book book) {
        LOGGER.log(Level.INFO, "REST - Updating book with ID: {0}", idString);
        Long id = validateAndParseId(idString);
        // Repository will check for duplicate titles on update
        UpdateResponse<Book> updateResponse = bookRepository.updateBook(id, book);
        LOGGER.log(Level.INFO, "REST - Book updated successfully. Fields updated: {0}", updateResponse.getFieldsUpdated());
        return Response.ok(updateResponse).build();
    }

    @DELETE
    public Response deleteBook(@PathParam("id") String idString) {
        LOGGER.log(Level.INFO, "REST - Deleting book with ID: {0}", idString);
        Long id = validateAndParseId(idString);
        DeleteResponse deleteResponse = bookRepository.deleteBook(id);
        LOGGER.log(Level.INFO, "REST - Book deleted successfully with ID: {0}", id);
        return Response.ok(deleteResponse).build();
    }

    private Long validateAndParseId(String idString) {
        // For /books/, idString will be empty string
        if (idString == null || idString.trim().isEmpty()) {
            LOGGER.warning("REST - Book ID is empty");
            throw new InvalidInputException("Book ID cannot be empty.");
        }
        
        try {
            Long id = Long.valueOf(idString.trim());
            LOGGER.log(Level.FINE, "REST - Parsed book ID: {0}", id);
            return id;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "REST - Invalid book ID format: {0}", idString);
            throw new InvalidInputException("Book ID must be a valid number.");
        }
    }
}