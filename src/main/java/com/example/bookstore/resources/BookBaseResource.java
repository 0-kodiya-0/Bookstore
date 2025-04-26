package com.example.bookstore.resources;

import com.example.bookstore.config.AppConfig;
import com.example.bookstore.exception.InvalidInputException;
import com.example.bookstore.models.Book;
import com.example.bookstore.repository.BookRepository;
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

@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookBaseResource {

    private BookRepository bookRepository = AppConfig.getBookRepository();

    @POST
    public Response createBook(Book book, @Context UriInfo uriInfo) {
        if (book.getAuthorId() == null) {
            throw new InvalidInputException("Author ID cannot be empty.");
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
}
