package com.flashcards.backend.flashcards.annotation;

import com.flashcards.backend.flashcards.dto.AIGenerateRequestDto;
import com.flashcards.backend.flashcards.dto.ErrorResponse;
import com.flashcards.backend.flashcards.dto.FlashcardDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Tag(name = "AI Operations", description = "AI-powered flashcard generation and content processing")
public @interface AIApiDocumentation {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(
            summary = "Generate flashcards from text using AI",
            description = "Uses AI to analyze text content and generate educational flashcards with questions and answers. " +
                    "Supports various content types including code blocks and technical material.",
            tags = {"AI Operations"}
    )
    @RequestBody(
            description = "AI generation request containing text content and generation parameters",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AIGenerateRequestDto.class),
                    examples = {
                            @ExampleObject(
                                    name = "Spring Framework Fundamentals",
                                    summary = "Generate flashcards from Spring Framework content with GPT-4o-mini",
                                    value = """
                                            {
                                              "deckId": "deck123",
                                              "userId": "user456",
                                              "text": "Spring Framework is a comprehensive programming and configuration model for Java applications. At its core, Spring is built around the concept of Inversion of Control (IoC) and Dependency Injection (DI). The Spring Container is responsible for managing the lifecycle of objects called beans. Beans are objects that are instantiated, assembled, and managed by the Spring IoC container. Configuration was traditionally XML-based, but modern Spring applications are annotation-driven. The @Component annotation is the main stereotype annotation that marks a class as a Spring component. @Service, @Repository, and @Controller are specialized versions of @Component that provide additional semantic meaning. @Service is used for business logic components, @Repository for data access components, and @Controller for web controllers. @Autowired enables automatic dependency injection by type. @Configuration classes define beans programmatically using @Bean methods. Component scanning with @ComponentScan automatically discovers and registers beans. Spring Boot further simplifies configuration with auto-configuration and starter dependencies.",
                                              "count": 8,
                                              "difficulty": "MEDIUM",
                                              "category": "Spring Framework",
                                              "model": "GPT_4O_MINI"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Advanced Content with Claude",
                                    summary = "Generate flashcards using Claude 3.5 Sonnet for complex topics",
                                    value = """
                                            {
                                              "deckId": "deck456",
                                              "userId": "user789",
                                              "text": "Advanced Spring concepts include Aspect-Oriented Programming (AOP) which allows for cross-cutting concerns like logging and security. Spring AOP uses proxies to implement aspects. @Transactional annotation enables declarative transaction management. Spring Boot provides auto-configuration that automatically configures Spring applications based on the dependencies present in the classpath.",
                                              "count": 5,
                                              "model": "CLAUDE_3_5_SONNET"
                                            }
                                            """
                            )
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully generated flashcards",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "array",
                                    implementation = FlashcardDto.class
                            ),
                            examples = @ExampleObject(
                                    name = "Generated Flashcards",
                                    summary = "AI-generated flashcards response",
                                    value = """
                                            [
                                              {
                                                "id": "fc1",
                                                "deckId": "deck123",
                                                "userId": "user456",
                                                "front": {
                                                  "text": "What is a class in Java?",
                                                  "type": "TEXT_ONLY",
                                                  "codeBlocks": []
                                                },
                                                "back": {
                                                  "text": "A class is a blueprint for creating objects that defines the structure and behavior of objects.",
                                                  "type": "TEXT_ONLY",
                                                  "codeBlocks": []
                                                },
                                                "hint": "Think of it as a template or blueprint",
                                                "tags": ["java", "oop", "fundamentals"],
                                                "difficulty": "EASY",
                                                "timesStudied": 0,
                                                "timesCorrect": 0,
                                                "timesIncorrect": 0,
                                                "createdAt": "2024-01-15T10:30:00Z",
                                                "updatedAt": "2024-01-15T10:30:00Z"
                                              }
                                            ]
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters or content",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Validation Error",
                                    value = """
                                            {
                                              "timestamp": "2024-01-15T10:30:00Z",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Text content is required for AI generation",
                                              "path": "/api/ai/flashcards/generate-text",
                                              "errorCode": "SVC_009"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Deck not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Deck Not Found",
                                    value = """
                                            {
                                              "timestamp": "2024-01-15T10:30:00Z",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Deck not found with id: deck123",
                                              "path": "/api/ai/flashcards/generate-text",
                                              "errorCode": "SVC_002"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "AI service rate limit exceeded",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Rate Limit Error",
                                    value = """
                                            {
                                              "timestamp": "2024-01-15T10:30:00Z",
                                              "status": 429,
                                              "error": "Too Many Requests",
                                              "message": "AI rate limit exceeded",
                                              "path": "/api/ai/flashcards/generate-text",
                                              "errorCode": "SVC_008"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "AI service error or internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "AI Service Error",
                                    value = """
                                            {
                                              "timestamp": "2024-01-15T10:30:00Z",
                                              "status": 500,
                                              "error": "Internal Server Error",
                                              "message": "AI service unavailable",
                                              "path": "/api/ai/flashcards/generate-text",
                                              "errorCode": "SVC_007"
                                            }
                                            """
                            )
                    )
            )
    })
    @interface GenerateFlashcardsFromText {}
}