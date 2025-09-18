package com.flashcards.backend.flashcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Flashcard deck information with metadata and statistics")
public class DeckDto {
    @Schema(description = "Unique identifier of the deck", example = "507f1f77bcf86cd799439012")
    private String id;

    @Schema(description = "Title of the deck", example = "JavaScript Fundamentals")
    private String title;

    @Schema(description = "Detailed description of the deck content", example = "Essential JavaScript concepts including closures, prototypes, and async programming")
    private String description;

    @Schema(description = "Category for organizing decks", example = "Programming")
    private String category;

    @Schema(description = "ID of the user who owns this deck", example = "507f1f77bcf86cd799439013")
    private String userId;

    @Schema(description = "Whether the deck is publicly visible to other users", example = "true")
    private boolean isPublic;

    @Schema(description = "Tags for categorizing and searching decks", example = "[\"javascript\", \"programming\", \"web-development\"]")
    private List<String> tags;

    @Schema(description = "Number of flashcards in this deck", example = "25")
    private int flashcardCount;

    @Schema(description = "Timestamp when the deck was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the deck was last updated")
    private LocalDateTime updatedAt;

    @Schema(description = "Timestamp when any card in this deck was last studied")
    private LocalDateTime lastStudiedAt;
}