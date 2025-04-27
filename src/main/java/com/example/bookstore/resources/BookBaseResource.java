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
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookBaseResource {

    private static final Logger LOGGER = Logger.getLogger(BookBaseResource.class.getName());
    private BookRepository bookRepository = AppConfig.getBookRepository();

    @POST
    public Response createBook(Book book, @Context UriInfo uriInfo) {
        LOGGER.log(Level.INFO, "REST - Creating book: {0}", book.getTitle());

        if (book.getAuthorId() == null) {
            LOGGER.warning("REST - Create book failed: Author ID is empty");
            throw new InvalidInputException("Author ID cannot be empty.");
        }

        if (book.getTitle() == null) {
            LOGGER.warning("REST - Create book failed: Title is empty");
            throw new InvalidInputException("Title cannot be empty");
        }
        
        // Validate publication year
        if (book.getPublicationYear() == null) {
            throw new InvalidInputException("Publication year cannot be empty.");
        }
        
        // Validate price
        if (book.getPrice() == null) {
            throw new InvalidInputException("Price cannot be empty.");
        }
        
        // Validate stock
        if (book.getStock() == null) {
            throw new InvalidInputException("Stock cannot be empty.");
        }

        // Book repository will check for duplicate titles internally
        Book createdBook = bookRepository.addBook(book);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(createdBook.getId())).build();

        LOGGER.log(Level.INFO, "REST - Book created successfully with ID: {0}", createdBook.getId());
        return Response.created(location).entity(createdBook).build();
    }

    @GET
    public List<Book> getAllBooks() {
        LOGGER.info("REST - Getting all books");
        List<Book> books = bookRepository.getAllBooks();
        LOGGER.log(Level.INFO, "REST - Retrieved {0} books", books.size());
        return books;
    }
}
