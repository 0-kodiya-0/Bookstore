package com.example.bookstore.bean;

import com.example.bookstore.models.Book;
import com.example.bookstore.models.Author;
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
public class BookBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Book> books;
    private Book book = new Book();
    private List<Author> authors;
    private String searchTerm;
    private Long filterAuthorId;
    private RestClient restClient = new RestClient();

    @PostConstruct
    public void init() {
        loadBooks();
        loadAuthors();

        // Check if there's a book ID in the request parameter
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap();
        String bookId = params.get("id");
        if (bookId != null && !bookId.isEmpty()) {
            try {
                Long id = Long.parseLong(bookId);
                loadBook(id);
            } catch (NumberFormatException e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Invalid book ID"));
            }
        }
    }

    public void loadBooks() {
        books = restClient.getAll("books", Book.class);
    }

    public void loadAuthors() {
        authors = restClient.getAll("authors", Author.class);
    }

    public String createBook() {
        try {
            restClient.post("books", book);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Book created successfully"));
            return "list?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
            return null;
        }
    }

    public void loadBook(Long id) {
        book = restClient.get("books/" + id, Book.class);
    }

    public String updateBook() {
        try {
            restClient.put("books/" + book.getId(), book);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Book updated successfully"));
            return "list?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
            return null;
        }
    }

    public void deleteBook(Long id) {
        try {
            restClient.delete("books/" + id);
            loadBooks();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Book deleted successfully"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public String getAuthorName(Long authorId) {
        if (authorId == null) {
            return "Unknown Author";
        }

        for (Author author : authors) {
            if (author.getId().equals(authorId)) {
                return author.getName();
            }
        }

        return "Unknown Author";
    }

    // Getters and setters
    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public Long getFilterAuthorId() {
        return filterAuthorId;
    }

    public void setFilterAuthorId(Long filterAuthorId) {
        this.filterAuthorId = filterAuthorId;
    }

// Add search and filter methods
    public String search() {
        // Implement search logic here
        return null; // or the navigation outcome
    }

    public String filter() {
        // Implement filter logic here
        return null; // or the navigation outcome
    }

}
