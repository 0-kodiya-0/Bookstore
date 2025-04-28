package com.example.bookstore.repository;

import com.example.bookstore.exception.AuthorNotFoundException;
import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.exception.InvalidInputException;
import com.example.bookstore.models.Book;
import com.example.bookstore.models.DeleteResponse;
import com.example.bookstore.models.UpdateResponse;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class BookRepository {

    private static final Map<Long, Book> books = new ConcurrentHashMap<>();
    private static final AtomicLong bookIdCounter = new AtomicLong(1);

    private AuthorRepository authorRepository;

    public void setAuthorRepository(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    // Create
    public synchronized Book addBook(Book book) {
        // Check for valid author
        if (!authorRepository.exists(book.getAuthorId())) {
            throw new AuthorNotFoundException(book.getAuthorId());
        }

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (book.getPublicationYear() > currentYear) {
            throw new InvalidInputException("Publication year cannot be in the future.");
        }

        if (book.getPrice() <= 0) {
            throw new InvalidInputException("Price cannot be negative or equal to 0.");
        }

        if (book.getStock() <= 0) {
            throw new InvalidInputException("Stock cannot be negative or equal to 0.");
        }

        if (isDuplicateTitle(book.getTitle())) {
            throw new InvalidInputException("A book with this title already exists.");
        }

        book.setId(bookIdCounter.getAndIncrement());
        books.put(book.getId(), book);
        return book;
    }

    // Check if a book with the same normalized title already exists
    public synchronized boolean isDuplicateTitle(String title) {
        if (title == null) {
            return false;
        }

        String normalizedNewTitle = normalizeTitle(title);

        return books.values().stream()
                .anyMatch(existingBook -> normalizeTitle(existingBook.getTitle()).equals(normalizedNewTitle));
    }

    // Normalize title by removing spaces, making lowercase, and removing symbols
    private String normalizeTitle(String title) {
        if (title == null) {
            return "";
        }

        // Remove all non-alphanumeric characters and convert to lowercase
        return title.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }

    // Read
    public List<Book> getAllBooks() {
        return new ArrayList<>(books.values());
    }

    public Book getBookById(Long id) {
        Book book = books.get(id);
        if (book == null) {
            throw new BookNotFoundException(id);
        }
        return book;
    }

    public List<Book> getBooksByAuthorId(Long authorId) {
        return books.values().stream()
                .filter(book -> book.getAuthorId().equals(authorId))
                .collect(Collectors.toList());
    }

    // Update
    public synchronized UpdateResponse<Book> updateBook(Long id, Book updatedBook) {
        Book currentBook = getBookById(id);
        int fieldsUpdated = 0;
        boolean updated = false;

        if (updatedBook.getTitle() != null) {
            if (updatedBook.getTitle().trim().isEmpty()) {
                throw new InvalidInputException("Book title cannot be empty.");
            } else if (!normalizeTitle(currentBook.getTitle()).equals(normalizeTitle(updatedBook.getTitle()))
                    && isDuplicateTitle(updatedBook.getTitle())) {
                throw new InvalidInputException("Update would create a duplicate title.");
            } else if (!currentBook.getTitle().equals(updatedBook.getTitle())) {
                currentBook.setTitle(updatedBook.getTitle());
                fieldsUpdated++;
                updated = true;
            }
        }

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (updatedBook.getPublicationYear() != null) {
            if (updatedBook.getPublicationYear() > currentYear) {
                throw new InvalidInputException("Publication year cannot be in the future.");
            } else if (!currentBook.getPublicationYear().equals(updatedBook.getPublicationYear())) {
                currentBook.setPublicationYear(updatedBook.getPublicationYear());
                fieldsUpdated++;
                updated = true;
            }
        }

        // Validate price
        if (updatedBook.getPrice() != null) {
            if (updatedBook.getPrice() <= 0) {
                throw new InvalidInputException("Price cannot be negative or equal to 0.");
            } else if (!currentBook.getPrice().equals(updatedBook.getPrice())) {
                currentBook.setPrice(updatedBook.getPrice());
                fieldsUpdated++;
                updated = true;
            }
        }

        // Validate stock
        if (updatedBook.getStock() != null) {
            if (updatedBook.getStock() < 0) {
                throw new InvalidInputException("Stock cannot be negative.");
            } else if (!currentBook.getStock().equals(updatedBook.getStock())) {
                currentBook.setStock(updatedBook.getStock());
                fieldsUpdated++;
                updated = true;
            }
        }

        if (updatedBook.getAuthorId() != null && !updatedBook.getAuthorId().equals(currentBook.getAuthorId())) {
            if (updatedBook.getAuthorId() < 0 || authorRepository.getAuthorById(updatedBook.getAuthorId()) == null) {
                throw new AuthorNotFoundException(updatedBook.getAuthorId());
            } else {
                currentBook.setAuthorId(updatedBook.getAuthorId());
                fieldsUpdated++;
                updated = true;
            }
        }

        return new UpdateResponse<>(currentBook, updated, fieldsUpdated);
    }

    // Delete
    public synchronized DeleteResponse deleteBook(Long id) {
        if (!exists(id)) {
            throw new BookNotFoundException(id);
        }

        books.remove(id);
        return new DeleteResponse(true, 1);
    }

    // Utility methods
    public boolean exists(Long id) {
        return books.containsKey(id);
    }

    public synchronized void reduceStock(Long id, int quantity) {
        Book book = getBookById(id);
        book.setStock(book.getStock() - quantity);
        books.put(id, book);
    }
}
