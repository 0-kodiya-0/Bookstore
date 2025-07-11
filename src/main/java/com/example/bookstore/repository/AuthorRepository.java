package com.example.bookstore.repository;

import com.example.bookstore.config.JPAConfig;
import com.example.bookstore.exception.AuthorNotFoundException;
import com.example.bookstore.exception.InvalidInputException;
import com.example.bookstore.models.Author;
import com.example.bookstore.models.Book;
import com.example.bookstore.models.DeleteResponse;
import com.example.bookstore.models.UpdateResponse;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

public class AuthorRepository {

    private static final Logger LOGGER = Logger.getLogger(AuthorRepository.class.getName());
    private BookRepository bookRepository;

    public void setBookRepository(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // Create
    public Author addAuthor(Author author) {
        if (author.getName() == null || author.getName().trim().isEmpty()) {
            throw new InvalidInputException("Author name cannot be empty.");
        }

        return JPAConfig.executeInTransaction(em -> {
            // Check for duplicate name
            if (isDuplicateName(author.getName(), em)) {
                throw new InvalidInputException("An author with this name already exists.");
            }

            em.persist(author);
            LOGGER.log(Level.INFO, "Author created with ID: {0}", author.getId());
            return author;
        });
    }

    // Check if a author with the same normalized name already exists
    private boolean isDuplicateName(String name, EntityManager em) {
        if (name == null) {
            return false;
        }

        String normalizedNewName = normalizeName(name);

        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(a) FROM Author a WHERE LOWER(REPLACE(REPLACE(REPLACE(a.name, ' ', ''), '-', ''), '_', '')) = :normalizedName", 
            Long.class
        );
        query.setParameter("normalizedName", normalizedNewName);
        
        return query.getSingleResult() > 0;
    }

    // Normalize name by removing spaces, making lowercase, and removing symbols
    private String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        // Remove all non-alphanumeric characters and convert to lowercase
        return name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }

    // Read
    public List<Author> getAllAuthors() {
        return JPAConfig.executeReadOnly(em -> {
            TypedQuery<Author> query = em.createQuery("SELECT a FROM Author a ORDER BY a.name", Author.class);
            List<Author> authors = query.getResultList();
            LOGGER.log(Level.INFO, "Retrieved {0} authors", authors.size());
            return authors;
        });
    }

    public Author getAuthorById(Long id) {
        return JPAConfig.executeReadOnly(em -> {
            Author author = em.find(Author.class, id);
            if (author == null) {
                throw new AuthorNotFoundException(id);
            }
            LOGGER.log(Level.INFO, "Retrieved author: {0}", author.getName());
            return author;
        });
    }

    // Update
    public UpdateResponse<Author> updateAuthor(Long id, Author updatedAuthor) {
        return JPAConfig.executeInTransaction(em -> {
            Author currentAuthor = em.find(Author.class, id);
            if (currentAuthor == null) {
                throw new AuthorNotFoundException(id);
            }

            int fieldsUpdated = 0;
            boolean updated = false;

            // Update name
            if (updatedAuthor.getName() != null) {
                if (updatedAuthor.getName().trim().isEmpty()) {
                    throw new InvalidInputException("Author name cannot be empty.");
                }
                
                // Check for duplicate name (excluding current author)
                if (!normalizeName(currentAuthor.getName()).equals(normalizeName(updatedAuthor.getName()))) {
                    TypedQuery<Long> query = em.createQuery(
                        "SELECT COUNT(a) FROM Author a WHERE a.id != :id AND LOWER(REPLACE(REPLACE(REPLACE(a.name, ' ', ''), '-', ''), '_', '')) = :normalizedName", 
                        Long.class
                    );
                    query.setParameter("id", id);
                    query.setParameter("normalizedName", normalizeName(updatedAuthor.getName()));
                    
                    if (query.getSingleResult() > 0) {
                        throw new InvalidInputException("Update would create a duplicate author name.");
                    }
                }
                
                if (!currentAuthor.getName().equals(updatedAuthor.getName())) {
                    currentAuthor.setName(updatedAuthor.getName());
                    fieldsUpdated++;
                    updated = true;
                }
            }

            // Update biography
            if (updatedAuthor.getBiography() != null) {
                if (updatedAuthor.getBiography().trim().isEmpty()) {
                    throw new InvalidInputException("Author biography cannot be empty.");
                }
                
                if (!currentAuthor.getBiography().equals(updatedAuthor.getBiography())) {
                    currentAuthor.setBiography(updatedAuthor.getBiography());
                    fieldsUpdated++;
                    updated = true;
                }
            }

            if (updated) {
                em.merge(currentAuthor);
                LOGGER.log(Level.INFO, "Author updated with ID: {0}, fields updated: {1}", new Object[]{id, fieldsUpdated});
            }

            return new UpdateResponse<>(currentAuthor, updated, fieldsUpdated);
        });
    }

    // Delete
    public DeleteResponse deleteAuthor(Long id) {
        return JPAConfig.executeInTransaction(em -> {
            Author author = em.find(Author.class, id);
            if (author == null) {
                throw new AuthorNotFoundException(id);
            }

            // Check if author has books before deletion
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(b) FROM Book b WHERE b.author.id = :authorId", Long.class
            );
            query.setParameter("authorId", id);
            
            if (query.getSingleResult() > 0) {
                throw new InvalidInputException("Cannot delete author with associated books. Remove the books first.");
            }

            em.remove(author);
            LOGGER.log(Level.INFO, "Author deleted with ID: {0}", id);
            return new DeleteResponse(true, 1);
        });
    }

    // Utility methods
    public boolean exists(Long id) {
        return JPAConfig.executeReadOnly(em -> {
            return em.find(Author.class, id) != null;
        });
    }
    
    // Search functionality
    public List<Author> searchAuthors(String searchTerm) {
        return JPAConfig.executeReadOnly(em -> {
            TypedQuery<Author> query = em.createQuery(
                "SELECT a FROM Author a WHERE LOWER(a.name) LIKE LOWER(:searchTerm) ORDER BY a.name", 
                Author.class
            );
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            
            List<Author> authors = query.getResultList();
            LOGGER.log(Level.INFO, "Found {0} authors matching search term: {1}", new Object[]{authors.size(), searchTerm});
            return authors;
        });
    }
}