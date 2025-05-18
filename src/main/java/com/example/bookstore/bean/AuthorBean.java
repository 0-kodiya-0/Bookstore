package com.example.bookstore.bean;

import com.example.bookstore.models.Author;
import com.example.bookstore.models.Book;
import com.example.bookstore.utils.RestClient;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@ManagedBean
@ViewScoped
public class AuthorBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Author> authors;
    private Author author = new Author();
    private List<Book> authorBooks;
    private String searchTerm;
    private RestClient restClient = new RestClient();

    @PostConstruct
    public void init() {
        loadAuthors();

        // Check if there's an author ID in the request parameter
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap();
        String authorId = params.get("id");
        if (authorId != null && !authorId.isEmpty()) {
            try {
                Long id = Long.parseLong(authorId);
                loadAuthor(id);
                loadAuthorBooks(id);
            } catch (NumberFormatException e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Invalid author ID"));
            }
        }
    }

    public void loadAuthors() {
        authors = restClient.getAll("authors", Author.class);
    }

    public String createAuthor() {
        try {
            restClient.post("authors", author);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Author created successfully"));
            return "list?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
            return null;
        }
    }

    public void loadAuthor(Long id) {
        author = restClient.get("authors/" + id, Author.class);
    }

    public void loadAuthorBooks(Long id) {
        authorBooks = restClient.getAll("authors/" + id + "/books", Book.class);
    }

    public String updateAuthor() {
        try {
            restClient.put("authors/" + author.getId(), author);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Author updated successfully"));
            return "list?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
            return null;
        }
    }

    public void deleteAuthor(Long id) {
        try {
            restClient.delete("authors/" + id);
            loadAuthors();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Author deleted successfully"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    // Getters and setters
    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public List<Book> getAuthorBooks() {
        return authorBooks;
    }

    public void setAuthorBooks(List<Book> authorBooks) {
        this.authorBooks = authorBooks;
    }

    // Add getter and setter
    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public String search() {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            // If search term is empty, just load all authors
            loadAuthors();
        } else {
            try {
                // Call the new search method in RestClient
                authors = restClient.search("authors", searchTerm, Author.class);

                if (authors.isEmpty()) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Search Results",
                                    "No authors found matching '" + searchTerm + "'"));
                } else {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Search Results",
                                    "Found " + authors.size() + " authors matching '" + searchTerm + "'"));
                }
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Search failed: " + e.getMessage()));
            }
        }
        return null; // Stay on the same page
    }
}
