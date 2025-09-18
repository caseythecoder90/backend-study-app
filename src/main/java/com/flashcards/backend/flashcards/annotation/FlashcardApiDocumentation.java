package com.flashcards.backend.flashcards.annotation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class FlashcardApiDocumentation {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Get flashcard by ID", description = "Retrieve a specific flashcard by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flashcard found successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Flashcard not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid flashcard ID format", content = @Content)
    })
    public @interface GetFlashcardById {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Get flashcards by deck", description = "Retrieve all flashcards belonging to a specific deck")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flashcards retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Deck not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid deck ID format", content = @Content)
    })
    public @interface GetFlashcardsByDeck {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Get flashcards by user", description = "Retrieve all flashcards owned by a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flashcards retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid user ID format", content = @Content)
    })
    public @interface GetFlashcardsByUser {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Search flashcards", description = "Search flashcards by content, tags, or other criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully",
                    content = @Content(mediaType = "application/json"))
    })
    public @interface SearchFlashcards {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Create new flashcard", description = "Create a new flashcard with content and metadata")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Flashcard created successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input data or validation errors", content = @Content),
            @ApiResponse(responseCode = "404", description = "Deck or user not found", content = @Content)
    })
    public @interface CreateFlashcard {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Update flashcard", description = "Update an existing flashcard's content and metadata")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flashcard updated successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Flashcard not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input data or validation errors", content = @Content)
    })
    public @interface UpdateFlashcard {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Delete flashcard", description = "Permanently delete a flashcard")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Flashcard deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Flashcard not found", content = @Content)
    })
    public @interface DeleteFlashcard {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Update study progress", description = "Record study session results for a flashcard")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Study progress updated successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Flashcard not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid study data", content = @Content)
    })
    public @interface UpdateStudyProgress {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Get flashcard count", description = "Get the total number of flashcards for a deck or user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class)))
    })
    public @interface GetFlashcardCount {}

    // Parameter annotations
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(description = "Unique identifier of the flashcard", example = "507f1f77bcf86cd799439011")
    public @interface FlashcardIdParam {}

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(description = "Unique identifier of the deck", example = "507f1f77bcf86cd799439012")
    public @interface DeckIdParam {}

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(description = "Unique identifier of the user", example = "507f1f77bcf86cd799439013")
    public @interface UserIdParam {}

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(description = "Search query string", example = "javascript functions")
    public @interface SearchQueryParam {}

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(description = "Tags to filter by", example = "programming,javascript")
    public @interface TagsParam {}

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(description = "Difficulty level filter", example = "MEDIUM")
    public @interface DifficultyParam {}

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(description = "Flashcard data for creation")
    public @interface CreateFlashcardBody {}

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(description = "Updated flashcard data")
    public @interface UpdateFlashcardBody {}

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(description = "Study session result data")
    public @interface StudyProgressBody {}
}