# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Architecture

This is a Spring Boot backend for a flashcard application using MongoDB as the database. The project follows a strict layered architecture:

1. **Controller Layer** - REST endpoints, request/response handling
2. **Service Layer** - Business logic, transaction management
3. **DAO Layer** - Data access abstraction, query logic
4. **Repository Layer** - MongoDB repository interfaces

## Coding Standards

### Layer Architecture Rules
- ALWAYS use all four layers: Controller → Service → DAO → Repository
- Controllers should only call Services
- Services should only call DAOs
- DAOs should only call Repositories
- Never skip layers or access repositories directly from services

### Exception Handling Strategy
- **DAO Layer**: Catch all database exceptions and wrap them in `DaoException` with appropriate `ErrorCode` enum? 
- **Service Layer**: Catch DAO exceptions and either handle them or wrap in `ServiceException`
- **Controller Layer**: Use `@RestControllerAdvice` for global exception handling
- All exceptions use `ErrorCode` enum for consistent error identification
- Use functional exception handling pattern with `executeWithExceptionHandling` method in DAOs

### Utility Classes Usage
Always use Apache Commons and Java utility classes:
- Use `StringUtils` from Apache Commons for string operations (never do direct null/empty checks)
- Use `BooleanUtils` from Apache Commons for ALL boolean operations (never do direct boolean checks like `if (flag)`)
  - Use `BooleanUtils.isTrue()`, `BooleanUtils.isFalse()`, `BooleanUtils.isNotTrue()`, `BooleanUtils.isNotFalse()`
  - Can be imported statically or used with class name
- Use `CollectionUtils` from Apache Commons for collection operations
- Use `IOUtils` from Apache Commons for I/O operations
- Use `Objects` from java.util for null-safe operations
- Use `Optional` for nullable return values

### Import Standards
- ALWAYS use static imports for constants (never wildcards)
- Import specific constants like `import static com.package.ErrorMessages.DAO_SAVE_ERROR;`
- Never use wildcard imports (`import static com.package.ErrorMessages.*;`)
- Keep imports organized: standard imports, then blank line, then static imports

### Constants Standards
- **API Paths**: Define directly in controllers using `@RequestMapping` annotations (e.g., `@RequestMapping("/api/auth")`). Do NOT extract API paths to constants files
- **String Literals in Methods**: Extract to constants when used as arguments or within method logic
- **Entity Names**: Use constants like `ENTITY_USER`, `ENTITY_DECK` in ErrorMessages for consistent entity naming
- **Error Messages**: All error messages go in `ErrorMessages.java` with placeholders for formatting
- For logging, do not use constants - use inline strings
- Constants should be `public static final` and follow UPPER_SNAKE_CASE naming
- **Constants File Organization** (one file per layer/concern):
  - `ErrorMessages.java` - All error message templates and entity name constants (e.g., `ENTITY_USER = "User"`)
  - `AuthConstants.java` - Authentication/authorization related constants
  - `SecurityConstants.java` - Security configuration constants
  - Additional constants files as needed for specific concerns (e.g., `JwtConstants.java`)
- **Import Pattern**: Use static imports for specific constants (never wildcards)
- **Usage Pattern**: Constants are used for:
  - Error message templates with placeholders
  - Entity names used in error messages
  - Business logic values (limits, thresholds, etc.)
  - Security/auth configuration values
  - Do NOT use for API paths or request mappings

### String Formatting Standards
- Use `String.formatted()` instance method instead of `String.format()` static method
- Example: `ERROR_MESSAGE.formatted(param1, param2)` instead of `String.format(ERROR_MESSAGE, param1, param2)`
- This provides cleaner, more functional-style code that's easier to read

### Validation Standards
- Use Jakarta Bean Validation annotations on DTOs for structural validation
- Combine with service-layer validation for business logic validation
- Always use `@Valid` on controller parameters to trigger validation
- GlobalExceptionHandler provides consistent error responses for validation failures
- Example validation levels:
  - **DTO Level**: `@NotBlank`, `@Size`, `@Email`, `@Pattern` for format/structure
  - **Service Level**: Business rules, uniqueness checks, authorization
  - **Nested DTOs**: Use `@Valid` to cascade validation to nested objects

### Functional Programming Approach
- Prefer streams and lambda expressions over traditional loops
- Use `Optional` instead of returning null
- Use method references where applicable
- Chain operations using functional style
- Avoid mutable state where possible
- Use `Supplier<T>` for lazy evaluation in exception handling

### Null Safety
- NEVER do direct null checks (e.g., `if (obj == null)`)
- Use `Objects.nonNull()`, `Objects.isNull()`, or `Optional`
- Use `StringUtils.isNotBlank()` instead of checking for null/empty strings
- Use `CollectionUtils.isNotEmpty()` for collections

### Lombok Usage Standards
- **ALWAYS use Lombok annotations** for DTOs and model classes to reduce boilerplate
- **Required Lombok annotations for DTOs**:
  - `@Data` - Generates getters, setters, toString, equals, and hashCode
  - `@Builder` - Provides builder pattern for object creation
  - `@NoArgsConstructor` - Generates no-args constructor (required for Jackson)
  - `@AllArgsConstructor` - Generates all-args constructor
- **For immutable DTOs**: Use `@Value` instead of `@Data`
- **For entities/models**: Consider using individual annotations (`@Getter`, `@Setter`) instead of `@Data` to avoid toString/equals issues
- **Never write manual getters/setters** when Lombok can generate them
- **For logging**: Use `@Slf4j` for SLF4J logger injection

## Commands

### Build and Run
```bash
# Start MongoDB
docker-compose up -d

# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run a specific test
./mvnw test -Dtest=FlashcardServiceTest

# Package as JAR
./mvnw clean package
```

### Development
```bash
# Skip tests during build
./mvnw clean install -DskipTests

# Run with debug mode
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

## Project Structure

```
src/main/java/com/flashcards/backend/flashcards/
├── controller/       # REST controllers
├── service/         # Business logic
├── dao/            # Data access objects
├── repository/     # MongoDB repositories
├── model/          # MongoDB documents/entities
├── dto/            # Data transfer objects
├── mapper/         # Entity-DTO mappers
├── config/         # Configuration classes
├── exception/      # Custom exceptions
└── util/           # Utility classes
```

## MongoDB Configuration

- Database: `flashcards`
- Collections: `users`, `decks`, `flashcards`, `study_sessions`
- Connection configured in `application.yml`
- Docker Compose setup available for local development

## Key Features to Implement

1. **Manual Flashcard Creation**: CRUD operations for flashcards with code block support
2. **AI-Generated Flashcards**: Integration point for AI service to generate cards from text
3. **Code Block Support**: Special handling for programming-related flashcards with syntax highlighting metadata
4. **Study Sessions**: Track user progress and learning statistics

## API Endpoint Structure

Follow RESTful conventions:
- GET `/api/decks` - List all decks
- GET `/api/decks/{id}` - Get specific deck
- POST `/api/decks` - Create deck
- PUT `/api/decks/{id}` - Update deck
- DELETE `/api/decks/{id}` - Delete deck
- POST `/api/flashcards/generate` - Generate flashcards from text using AI

## API Documentation Standards

ALL APIs MUST include comprehensive OpenAPI/Swagger annotations:

### Custom Documentation Annotations
- ALWAYS use custom annotation classes for REST controllers with extensive Swagger documentation
- Create dedicated annotation files in the `annotation` package for each controller
- This keeps controller code clean and separates documentation concerns
- Follow the pattern established in existing annotation files (e.g., `ApiDocumentation.java`, `DeckApiDocumentation.java`)

### Required Annotations for Controllers
- `@Tag` - Categorize related endpoints
- `@Operation` - Describe what the endpoint does
- `@ApiResponse` - Document all possible response codes and types
- `@Parameter` - Document path/query parameters with examples
- `@RequestBody` - Document request body structure

### Required Annotations for DTOs
- `@Schema` - Describe the DTO purpose and provide examples
- Each field must have `@Schema` with description and example values
- Use `@Size`, `@NotNull`, `@NotBlank` validation annotations with meaningful messages

### Documentation Guidelines
- Provide clear, concise descriptions for all endpoints
- Include example request/response payloads
- Document all error scenarios with appropriate HTTP status codes
- Use consistent error response format across all endpoints
- Include realistic example values in schemas

## Testing Requirements

- Unit tests for all service methods
- Integration tests for DAO layer
- Controller tests using MockMvc
- Use `@DataMongoTest` for repository tests
- Maintain minimum 80% code coverage
- always use custom annotations as found in annotations package for things like REST controllers where there is a lot of swagger documentation. this helps in keeping the code clean. change this for the auth controller