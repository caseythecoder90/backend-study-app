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

public class DeckApiDocumentation {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Get deck by ID", description = "Retrieve a specific deck by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deck found successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Deck not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid deck ID format", content = @Content)
    })
    public @interface GetDeckById {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Get decks by user", description = "Retrieve all decks owned by a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Decks retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid user ID format", content = @Content)
    })
    public @interface GetDecksByUser {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Get public decks", description = "Retrieve all publicly shared decks available to all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Public decks retrieved successfully",
                    content = @Content(mediaType = "application/json"))
    })
    public @interface GetPublicDecks {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Get decks by category", description = "Retrieve all decks belonging to a specific category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Decks retrieved successfully",
                    content = @Content(mediaType = "application/json"))
    })
    public @interface GetDecksByCategory {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Search decks by tag", description = "Find decks that contain a specific tag")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully",
                    content = @Content(mediaType = "application/json"))
    })
    public @interface SearchDecksByTag {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Get user decks by visibility", description = "Retrieve user's decks filtered by public/private visibility")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Decks retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    public @interface GetDecksByVisibility {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Get all decks", description = "Retrieve all available decks (admin function)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All decks retrieved successfully",
                    content = @Content(mediaType = "application/json"))
    })
    public @interface GetAllDecks {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Create new deck", description = "Create a new flashcard deck with metadata and settings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Deck created successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input data or validation errors", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Deck name already exists for user", content = @Content)
    })
    public @interface CreateDeck {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Update deck", description = "Update an existing deck's information and settings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deck updated successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Deck not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input data or validation errors", content = @Content),
            @ApiResponse(responseCode = "403", description = "Not authorized to update this deck", content = @Content)
    })
    public @interface UpdateDeck {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Delete deck", description = "Permanently delete a deck and all its flashcards")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deck deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Deck not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Not authorized to delete this deck", content = @Content)
    })
    public @interface DeleteDeck {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Get deck statistics", description = "Get comprehensive statistics about a deck including study progress")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Deck not found", content = @Content)
    })
    public @interface GetDeckStats {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Get total deck count", description = "Get the total number of decks in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class)))
    })
    public @interface GetDeckCount {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Clone public deck", description = "Create a personal copy of a public deck")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Deck cloned successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Source deck not found or not public", content = @Content),
            @ApiResponse(responseCode = "400", description = "Cannot clone private deck", content = @Content)
    })
    public @interface CloneDeck {}

    // Parameter annotations
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
    @Parameter(description = "Category name for filtering decks", example = "Programming")
    public @interface CategoryParam {}

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(description = "Tag to search for in deck tags", example = "javascript")
    public @interface TagParam {}

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(description = "Visibility filter: true for public, false for private", example = "true")
    public @interface VisibilityParam {}

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(description = "Deck data for creation")
    public @interface CreateDeckBody {}

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(description = "Updated deck data")
    public @interface UpdateDeckBody {}

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(description = "ID of the deck to clone", example = "507f1f77bcf86cd799439014")
    public @interface SourceDeckIdParam {}

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(description = "ID of the user who will own the cloned deck", example = "507f1f77bcf86cd799439015")
    public @interface TargetUserIdParam {}
}