package com.example.bookstore.resources;

import com.example.bookstore.config.AppConfig;
import com.example.bookstore.exception.AuthorNotFoundException;
import com.example.bookstore.exception.InvalidInputException;
import com.example.bookstore.models.Book;
import com.example.bookstore.models.DeleteResponse;
import com.example.bookstore.models.UpdateResponse;
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
@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookResource {
    
    private BookRepository bookRepository = AppConfig.getBookRepository();
            
    @POST
    public Response createBook(Book book, @Context UriInfo uriInfo) {
        if (book.getAuthorId() == null) {
            throw new AuthorNotFoundException(book.getAuthorId());
        }
        
        if (book.getTitle() == null) {
            throw new InvalidInputException("Title cannot be empty");
        }
        
        // Book repository will check for duplicate titles internally
        Book createdBook = bookRepository.addBook(book);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(createdBook.getId())).build();
        
        return Response.created(location).entity(createdBook).build();
    }
    
    @GET
    public List<Book> getAllBooks() {
        return bookRepository.getAllBooks();
    }
    
    @GET
    @Path("/{id}")
    public Book getBookById(@PathParam("id") Long id) {
        return bookRepository.getBookById(id);
    }
    
    @PUT
    @Path("/{id}")
    public Response updateBook(@PathParam("id") Long id, Book book) {
        // Repository will check for duplicate titles on update
        UpdateResponse<Book> updateResponse = bookRepository.updateBook(id, book);
        return Response.ok(updateResponse).build();
    }
    
    @DELETE
    @Path("/{id}")
    public Response deleteBook(@PathParam("id") Long id) {
        DeleteResponse deleteResponse = bookRepository.deleteBook(id);
        return Response.ok(deleteResponse).build();
    }
}