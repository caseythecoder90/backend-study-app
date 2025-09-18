package com.flashcards.backend.flashcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data required to create a new flashcard deck")
public class CreateDeckDto {
    @Schema(description = "Title of the deck (1-100 characters)", example = "JavaScript Fundamentals")
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters")
    private String title;

    @Schema(description = "Detailed description of the deck content (optional, max 500 characters)", example = "Essential JavaScript concepts including closures, prototypes, and async programming")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Schema(description = "Category for organizing decks (optional, max 50 characters)", example = "Programming")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    @Schema(description = "ID of the user creating this deck", example = "507f1f77bcf86cd799439013")
    @NotBlank(message = "User ID is required")
    private String userId;

    @Schema(description = "Whether the deck should be publicly visible to other users", example = "false")
    private boolean isPublic;

    @Schema(description = "Tags for categorizing and searching (max 10 tags, 1-30 chars each)", example = "[\"javascript\", \"programming\", \"web-development\"]")
    @Size(max = 10, message = "Maximum 10 tags allowed")
    private List<@Size(min = 1, max = 30, message = "Each tag must be between 1 and 30 characters") String> tags;
}