# Bookstore Application v1.0.0

## Release Notes

This is the initial release of the Bookstore Application, providing core e-commerce functionality for an online bookstore.

### Current Features
- RESTful API backend with JAX-RS
- Responsive frontend UI with JSF and Tailwind CSS
- User authentication with JWT
- Book and author management
- Shopping cart functionality
- Order processing and history

### Technical Implementation
- In-memory data storage (non-persistent)
- Clean client-server architecture
- Exception handling with appropriate HTTP status codes
- Mobile-responsive design

### Known Limitations
- **Data Persistence**: Currently using in-memory storage. Data will not persist between application restarts.
- **User Roles**: Basic role implementation (customer/admin) without granular permissions.
- **Search Functionality**: Basic search implementation without advanced filtering options.
- **Image Handling**: Placeholder images used instead of actual book/author images.

### Upcoming in Future Releases
- Persistent database storage (MySQL/PostgreSQL)
- Enhanced search with filters and sorting
- Image upload for books and authors
- User roles and permissions system
- Review and rating system
- Recommendation engine
- Payment gateway integration
- Email notifications

## Installation

1. Download the `bookstore-1.0.0.war` file from the releases page
2. Deploy to a servlet container like Tomcat 9.x
3. Access at `http://localhost:8080/bookstore/`

## Default Credentials

For testing purposes, you can register a new account or use the following credentials:

- **Admin Access**:
  - Email: admin@bookstore.com
  - Password: admin123

## Feedback and Contributions

We welcome feedback and contributions to help improve the Bookstore Application. Please submit issues and pull requests through our repository.

---

Released on May 20, 2025 | MIT License