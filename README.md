# Library Kata - Implementation Details

## Overview
This library application provides the following features:

1. **Add a new book to the catalog** – Allow adding books to the library system.
2. **Borrow a book** – Users can borrow books from the library.
3. **Return a book** – Users can return borrowed books.
4. **Print a list of borrowed books** – Users can view a list of all the books they have borrowed.

## Design Decisions

### Simplicity
- **RESTful API**: The library uses RESTful API as it is the most common standard for HTTP communication between services. This ensures interoperability, simplicity, and scalability.
- **Spring Boot**: I chose Spring Boot as the framework due to its efficiency and power within the Java ecosystem. It offers extensive features like dependency injection, built-in security, and integration with various libraries, making development faster and simpler.
- **User Authentication**: User authentication is excluded to keep the application simple. It can be added as a separate service in the future if needed.
- **H2 In-Memory Database**: I used H2 for its simplicity and efficiency. It acts like a real SQL database but runs in-memory, making it ideal for development, testing, and environments where persistence isn't required.
- **Swagger**: Swagger is used as both a documentation tool and a UI for API testing. It simplifies interaction with the application and serves as a reference for API endpoints.

### Maintainability
- **Readable Code**: The code is written to be clear and readable, with meaningful naming conventions. Magic values are avoided, and code is divided into clear layers (e.g., controller, service, repository).
- **Tests**: Both unit and integration tests are included to ensure the application behaves as expected and to prevent regressions.
- **Documentation**: OpenAPI (Swagger) is used to document the API, making it easier for developers to understand and use the service.
- **Separation of Concerns**: The application follows the principle of separation of concerns. Each layer (controller, service, repository) has a distinct responsibility, which improves maintainability and makes the code easier to extend.

### Reliability
- **Input Validation**: The application validates inputs (e.g., ensuring the book is available to borrow, verifying the user exists) to avoid invalid data from affecting the system.
- **Error Handling**: A global `ExceptionHandler` is used to handle all API errors consistently. It provides clear error messages and proper HTTP status codes, ensuring a smooth user experience.
- **Transactions and Optimistic Locking**: To prevent race conditions when multiple users try to borrow or return the same book simultaneously, optimistic locking is used. This ensures data integrity without introducing unnecessary complexity.
- **Test Coverage**: Tests simulate failures and invalid input scenarios to ensure the system behaves as expected even when unexpected situations occur.

### Scalability
- **Pagination**: The listing of books, supports pagination to handle large datasets efficiently.
- **Statelessness**: The application is stateless, meaning it doesn't maintain session information on the server. This makes it easier to scale the application horizontally in the future.
- **Future Enhancements**:
    - **Caching**: Caching is recommended for use in the future to optimize repeated queries, such as listing books.
    - **Containerization**: The application can be containerized using Docker for easier deployment and scaling across different environments.

### Performance
- **Reactive Stack**: The application uses a reactive stack of technologies, specifically **Spring WebFlux**, to provide non-blocking, asynchronous behavior. This enables the application to handle high loads and concurrent requests efficiently. Reactive programming is particularly beneficial for I/O-heavy applications like this one, where operations like database access, network calls, and external APIs can be executed concurrently without blocking threads.
- **Model Efficiency**: The model has been designed to support high performance. By leveraging optimistic locking to avoid blocking when concurrent users interact with the same resources (e.g., borrowing a book), the application ensures that race conditions are resolved efficiently without locking the database.
- **Asynchronous Database Access**: The use of **Spring Data R2DBC** ensures asynchronous, non-blocking interactions with the database. This allows the system to scale well with high concurrency while minimizing thread contention and reducing response time.
