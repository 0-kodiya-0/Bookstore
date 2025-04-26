package com.example.bookstore.resources;

import com.example.bookstore.config.AppConfig;
import com.example.bookstore.exception.InvalidInputException;
import com.example.bookstore.models.Book;
import com.example.bookstore.models.DeleteResponse;
import com.example.bookstore.models.UpdateResponse;
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

@Path("/books/{id: [^/]*}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookIdResource {

    private BookRepository bookRepository = AppConfig.getBookRepository();

    @GET
    public Book getBookById(@PathParam("id") String idString) {
        Long id = validateAndParseId(idString);
        return bookRepository.getBookById(id);
    }

    @PUT
    public Response updateBook(@PathParam("id") String idString, Book book) {
        Long id = validateAndParseId(idString);
        // Repository will check for duplicate titles on update
        UpdateResponse<Book> updateResponse = bookRepository.updateBook(id, book);
        return Response.ok(updateResponse).build();
    }

    @DELETE
    public Response deleteBook(@PathParam("id") String idString) {
        Long id = validateAndParseId(idString);
        DeleteResponse deleteResponse = bookRepository.deleteBook(id);
        return Response.ok(deleteResponse).build();
    }

    private Long validateAndParseId(String idString) {
        // For /books/, idString will be empty string
        if (idString == null || idString.trim().isEmpty()) {
            throw new InvalidInputException("Book ID cannot be empty.");
        }
        
        try {
            return Long.parseLong(idString.trim());
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Book ID must be a valid number.");
        }
    }
}