package com.example.bookstore.repository;

import com.example.bookstore.config.JPAConfig;
import com.example.bookstore.exception.AuthorNotFoundException;
import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.exception.InvalidInputException;
import com.example.bookstore.models.Author;
import com.example.bookstore.models.Book;
import com.example.bookstore.models.DeleteResponse;
import com.example.bookstore.models.UpdateResponse;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

public class BookRepository {

    private static final Logger LOGGER = Logger.getLogger(BookRepository.class.getName());
    private AuthorRepository authorRepository;

    public void setAuthorRepository(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    // Create
    public Book addBook(Book book) {
        return JPAConfig.executeInTransaction(em -> {
            // Validate basic fields
            if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
                throw new InvalidInputException("Book title cannot be empty.");
            }
            
            if (book.getPublicationYear() == null) {
                throw new InvalidInputException("Publication year cannot be empty.");
            }
            
            if (book.getPrice() == null || book.getPrice() <= 0) {
                throw new InvalidInputException("Price must be greater than 0.");
            }
            
            if (book.getStock() == null || book.getStock() < 0) {
                throw new InvalidInputException("Stock cannot be negative.");
            }

            // Validate publication year
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            if (book.getPublicationYear() > currentYear) {
                throw new InvalidInputException("Publication year cannot be in the future.");
            }

            // Check for duplicate title
            if (isDuplicateTitle(book.getTitle(), em)) {
                throw new InvalidInputException("A book with this title already exists.");
            }

            // Handle author relationship
            Author author;
            if (book.getAuthor() != null) {
                // Author object is provided
                author = em.find(Author.class, book.getAuthor().getId());
                if (author == null) {
                    throw new AuthorNotFoundException(book.getAuthor().getId());
                }
            } else {
                // Handle legacy authorId field (for backward compatibility)
                Long authorId = book.getAuthorId();
                if (authorId == null) {
                    throw new InvalidInputException("Author must be specified.");
                }
                author = em.find(Author.class, authorId);
                if (author == null) {
                    throw new AuthorNotFoundException(authorId);
                }
            }

            book.setAuthor(author);
            em.persist(book);
            LOGGER.log(Level.INFO, "Book created with ID: {0}", book.getId());
            return book;
        });
    }

    // Check if a book with the same normalized title already exists
    private boolean isDuplicateTitle(String title, EntityManager em) {
        if (title == null) {
            return false;
        }

        String normalizedNewTitle = normalizeTitle(title);

        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(b) FROM Book b WHERE LOWER(REPLACE(REPLACE(REPLACE(b.title, ' ', ''), '-', ''), '_', '')) = :normalizedTitle", 
            Long.class
        );
        query.setParameter("normalizedTitle", normalizedNewTitle);
        
        return query.getSingleResult() > 0;
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
        return JPAConfig.executeReadOnly(em -> {
            TypedQuery<Book> query = em.createQuery("SELECT b FROM Book b ORDER BY b.title", Book.class);
            List<Book> books = query.getResultList();
            LOGGER.log(Level.INFO, "Retrieved {0} books", books.size());
            return books;
        });
    }

    public Book getBookById(Long id) {
        return JPAConfig.executeReadOnly(em -> {
            Book book = em.find(Book.class, id);
            if (book == null) {
                throw new BookNotFoundException(id);
            }
            LOGGER.log(Level.INFO, "Retrieved book: {0}", book.getTitle());
            return book;
        });
    }

    public List<Book> getBooksByAuthorId(Long authorId) {
        return JPAConfig.executeReadOnly(em -> {
            TypedQuery<Book> query = em.createQuery(
                "SELECT b FROM Book b WHERE b.author.id = :authorId ORDER BY b.title", 
                Book.class
            );
            query.setParameter("authorId", authorId);
            
            List<Book> books = query.getResultList();
            LOGGER.log(Level.INFO, "Retrieved {0} books for author ID: {1}", new Object[]{books.size(), authorId});
            return books;
        });
    }

    // Update
    public UpdateResponse<Book> updateBook(Long id, Book updatedBook) {
        return JPAConfig.executeInTransaction(em -> {
            Book currentBook = em.find(Book.class, id);
            if (currentBook == null) {
                throw new BookNotFoundException(id);
            }

            int fieldsUpdated = 0;
            boolean updated = false;

            // Update title
            if (updatedBook.getTitle() != null) {
                if (updatedBook.getTitle().trim().isEmpty()) {
                    throw new InvalidInputException("Book title cannot be empty.");
                }
                
                // Check for duplicate title (excluding current book)
                if (!normalizeTitle(currentBook.getTitle()).equals(normalizeTitle(updatedBook.getTitle()))) {
                    TypedQuery<Long> query = em.createQuery(
                        "SELECT COUNT(b) FROM Book b WHERE b.id != :id AND LOWER(REPLACE(REPLACE(REPLACE(b.title, ' ', ''), '-', ''), '_', '')) = :normalizedTitle", 
                        Long.class
                    );
                    query.setParameter("id", id);
                    query.setParameter("normalizedTitle", normalizeTitle(updatedBook.getTitle()));
                    
                    if (query.getSingleResult() > 0) {
                        throw new InvalidInputException("Update would create a duplicate title.");
                    }
                }
                
                if (!currentBook.getTitle().equals(updatedBook.getTitle())) {
                    currentBook.setTitle(updatedBook.getTitle());
                    fieldsUpdated++;
                    updated = true;
                }
            }

            // Update publication year
            if (updatedBook.getPublicationYear() != null) {
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                if (updatedBook.getPublicationYear() > currentYear) {
                    throw new InvalidInputException("Publication year cannot be in the future.");
                }
                
                if (!currentBook.getPublicationYear().equals(updatedBook.getPublicationYear())) {
                    currentBook.setPublicationYear(updatedBook.getPublicationYear());
                    fieldsUpdated++;
                    updated = true;
                }
            }

            // Update price
            if (updatedBook.getPrice() != null) {
                if (updatedBook.getPrice() <= 0) {
                    throw new InvalidInputException("Price must be greater than 0.");
                }
                
                if (!currentBook.getPrice().equals(updatedBook.getPrice())) {
                    currentBook.setPrice(updatedBook.getPrice());
                    fieldsUpdated++;
                    updated = true;
                }
            }

            // Update stock
            if (updatedBook.getStock() != null) {
                if (updatedBook.getStock() < 0) {
                    throw new InvalidInputException("Stock cannot be negative.");
                }
                
                if (!currentBook.getStock().equals(updatedBook.getStock())) {
                    currentBook.setStock(updatedBook.getStock());
                    fieldsUpdated++;
                    updated = true;
                }
            }

            // Update ISBN
            if (updatedBook.getIsbn() != null && !updatedBook.getIsbn().equals(currentBook.getIsbn())) {
                currentBook.setIsbn(updatedBook.getIsbn());
                fieldsUpdated++;
                updated = true;
            }

            // Update author
            if (updatedBook.getAuthor() != null) {
                Author newAuthor = em.find(Author.class, updatedBook.getAuthor().getId());
                if (newAuthor == null) {
                    throw new AuthorNotFoundException(updatedBook.getAuthor().getId());
                }
                
                if (!currentBook.getAuthor().getId().equals(newAuthor.getId())) {
                    currentBook.setAuthor(newAuthor);
                    fieldsUpdated++;
                    updated = true;
                }
            } else if (updatedBook.getAuthorId() != null && !updatedBook.getAuthorId().equals(currentBook.getAuthor().getId())) {
                // Handle legacy authorId field
                Author newAuthor = em.find(Author.class, updatedBook.getAuthorId());
                if (newAuthor == null) {
                    throw new AuthorNotFoundException(updatedBook.getAuthorId());
                }
                currentBook.setAuthor(newAuthor);
                fieldsUpdated++;
                updated = true;
            }

            if (updated) {
                em.merge(currentBook);
                LOGGER.log(Level.INFO, "Book updated with ID: {0}, fields updated: {1}", new Object[]{id, fieldsUpdated});
            }

            return new UpdateResponse<>(currentBook, updated, fieldsUpdated);
        });
    }

    // Delete
    public DeleteResponse deleteBook(Long id) {
        return JPAConfig.executeInTransaction(em -> {
            Book book = em.find(Book.class, id);
            if (book == null) {
                throw new BookNotFoundException(id);
            }

            em.remove(book);
            LOGGER.log(Level.INFO, "Book deleted with ID: {0}", id);
            return new DeleteResponse(true, 1);
        });
    }

    // Utility methods
    public boolean exists(Long id) {
        return JPAConfig.executeReadOnly(em -> {
            return em.find(Book.class, id) != null;
        });
    }

    public void reduceStock(Long id, int quantity) {
        JPAConfig.executeInTransaction(em -> {
            Book book = em.find(Book.class, id);
            if (book == null) {
                throw new BookNotFoundException(id);
            }
            
            book.setStock(book.getStock() - quantity);
            em.merge(book);
            LOGGER.log(Level.INFO, "Reduced stock for book ID: {0} by {1}", new Object[]{id, quantity});
        });
    }
    
    // Search functionality
    public List<Book> searchBooks(String searchTerm) {
        return JPAConfig.executeReadOnly(em -> {
            TypedQuery<Book> query = em.createQuery(
                "SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(:searchTerm) ORDER BY b.title", 
                Book.class
            );
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            
            List<Book> books = query.getResultList();
            LOGGER.log(Level.INFO, "Found {0} books matching search term: {1}", new Object[]{books.size(), searchTerm});
            return books;
        });
    }
}