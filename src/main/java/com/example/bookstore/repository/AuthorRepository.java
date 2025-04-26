package com.example.bookstore.repository;

import com.example.bookstore.exception.AuthorNotFoundException;
import com.example.bookstore.exception.InvalidInputException;
import com.example.bookstore.models.Author;
import com.example.bookstore.models.Book;
import com.example.bookstore.models.DeleteResponse;
import com.example.bookstore.models.UpdateResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author HP
 */
public class AuthorRepository {

    private static final Map<Long, Author> authors = new ConcurrentHashMap<>();
    private static final AtomicLong authorIdCounter = new AtomicLong(1);
    private BookRepository bookRepository;

    public void setBookRepository(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // Create
    public synchronized Author addAuthor(Author author) {
        if (author.getName().trim().isEmpty()) {
            throw new InvalidInputException("Author name cannot be empty.");
        }

        if (isDuplicateName(author.getName())) {
            throw new InvalidInputException("A author with this name already exists.");
        }

        author.setId(authorIdCounter.getAndIncrement());
        authors.put(author.getId(), author);
        return author;
    }

    // Check if a book with the same normalized title already exists
    public synchronized boolean isDuplicateName(String name) {
        if (name == null) {
            return false;
        }

        String normalizedNewTitle = normalizeName(name);

        return authors.values().stream()
                .anyMatch(existingBook -> normalizeName(existingBook.getName()).equals(normalizedNewTitle));
    }

    // Normalize title by removing spaces, making lowercase, and removing symbols
    private String normalizeName(String name) {
        if (name == null) {
            return "";
        }

        // Remove all non-alphanumeric characters and convert to lowercase
        return name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }

    // Read
    public List<Author> getAllAuthors() {
        return new ArrayList<>(authors.values());
    }

    public Author getAuthorById(Long id) {
        Author author = authors.get(id);
        if (author == null) {
            throw new AuthorNotFoundException(id);
        }
        return author;
    }

    public synchronized UpdateResponse<Author> updateAuthor(Long id, Author updatedAuthor) {
        Author currentAuthor = getAuthorById(id);
        int fieldsUpdated = 0;
        boolean updated = false;

        if (updatedAuthor.getName() != null && !updatedAuthor.getName().trim().isEmpty()) {
            if (!normalizeName(currentAuthor.getName()).equals(normalizeName(updatedAuthor.getName()))
                    && isDuplicateName(updatedAuthor.getName())) {
                throw new InvalidInputException("Update would create a duplicate author names.");
            } else if (!currentAuthor.getName().equals(updatedAuthor.getName())) {
                currentAuthor.setName(updatedAuthor.getName());
                fieldsUpdated++;
                updated = true;
            }
        }

        if (updatedAuthor.getBiography() != null && !updatedAuthor.getBiography().trim().isEmpty()) {
            if (!currentAuthor.getBiography().equals(updatedAuthor.getBiography())) {
                currentAuthor.setBiography(updatedAuthor.getBiography());
                fieldsUpdated++;
                updated = true;
            }
        }

        return new UpdateResponse<>(currentAuthor, updated, fieldsUpdated);
    }

    public synchronized DeleteResponse deleteAuthor(Long id) {
        if (!exists(id)) {
            throw new AuthorNotFoundException(id);
        }

        // Check if author has books before deletion
        List<Book> authorBooks = bookRepository.getBooksByAuthorId(id);
        if (!authorBooks.isEmpty()) {
            throw new InvalidInputException("Cannot delete author with associated books. Remove the books first.");
        }

        authors.remove(id);
        return new DeleteResponse(true, 1);
    }

    // Utility methods
    public boolean exists(Long id) {
        return authors.containsKey(id);
    }
}
