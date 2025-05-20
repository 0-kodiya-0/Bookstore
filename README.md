# Bookstore Web Application

An online bookstore application with a RESTful API backend and JSF frontend, providing a complete e-commerce solution for book management, user authentication, shopping cart functionality, and order processing.

## Project Overview

The Bookstore Web Application is a Java-based web application designed to showcase client-server architecture using RESTful principles. It uses JAX-RS for the backend API and JSF (JavaServer Faces) with Tailwind CSS for the frontend user interface. The application also implements an authentication system using JWT tokens.

## Architecture

### Backend (API)
- **JAX-RS REST API**: Handles all data operations and business logic
- **Repository Pattern**: Manages in-memory data storage (expandable to database)
- **Exception Mappers**: Provides standardized error responses
- **JWT Authentication**: Secures endpoints with token-based authentication

### Frontend
- **JSF Framework**: Provides server-side UI components and page navigation
- **Tailwind CSS**: Modern, responsive styling
- **RESTful Client**: Communicates with the backend API
- **Session Management**: Manages user authentication state

## Features

### Customer-Facing Features
- **User Authentication**: Register, login, and profile management
- **Book Browsing**: View all books with search and filter capabilities
- **Author Exploration**: Browse authors and their published books
- **Shopping Cart**: Add, update, and remove items
- **Order Management**: Place orders and view order history

### Admin Features
- **Content Management**: Add, edit, and delete books and authors
- **Inventory Control**: Manage book stock levels
- **User Management**: View and manage customer accounts

## Technical Highlights

- **RESTful API Design**: Following best practices for resource naming and operations
- **Custom Exception Handling**: Proper error reporting with appropriate HTTP status codes
- **JWT Token Authentication**: Secure API access
- **Responsive UI**: Mobile-friendly design using modern CSS framework
- **Clean Code Organization**: Well-structured package and class hierarchy

## Project Structure

```
com.example.bookstore/
├── bean/                 # JSF backing beans
├── config/               # Application configuration
├── exception/            # Custom exceptions
│   └── mapper/           # Exception mappers for REST responses
├── filters/              # Request and response filters
├── models/               # Data models and DTOs
├── repository/           # Data access layer
├── resources/            # REST API endpoints
├── utils/                # Utility classes
└── validator/            # Custom JSF validators
```

## Setup and Installation

### Prerequisites
- Java 8 or higher
- Apache Maven 3.5 or higher
- Apache Tomcat 9.x or higher (or similar servlet container)

### Build and Deploy
1. Clone the repository:
   ```
   git clone https://github.com/yourusername/bookstore.git
   ```

2. Build the application using Maven:
   ```
   cd bookstore
   mvn clean package
   ```

3. Deploy the WAR file to your servlet container:
   - Copy `target/bookstore-1.0-SNAPSHOT.war` to your Tomcat `webapps` directory
   - Or deploy through your servlet container's management interface

4. Access the application:
   ```
   http://localhost:8080/bookstore/
   ```

## API Documentation

The RESTful API provides endpoints for managing books, authors, customers, shopping carts, and orders.

### Main Endpoints

- `/api/books` - Book management
- `/api/authors` - Author management
- `/api/customers` - Customer accounts
- `/api/customers/{id}/cart` - Shopping cart operations
- `/api/customers/{id}/orders` - Order management
- `/api/auth` - Authentication services

### Authentication

Protected endpoints require an Authorization header with a valid JWT token:

```
Authorization: Bearer <jwt_token>
```

A token can be obtained via the `/api/auth/login` endpoint.

## Contributors

- Sanithu Jayakody - Initial development

## License

This project is licensed under the MIT License - see the LICENSE file for details.

```
MIT License

Copyright (c) 2025 Sanithu Jayakody

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Latest Release

The latest release is [v1.0.0](docs/releases/v1.0.0/RELEASE-NOTES.md).
Download the WAR file from our [GitHub Releases](https://github.com/yourusername/bookstore/releases/tag/v1.0.0) page.