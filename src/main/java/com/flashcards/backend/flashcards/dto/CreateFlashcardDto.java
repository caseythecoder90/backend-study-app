package com.flashcards.backend.flashcards.dto;

import com.flashcards.backend.flashcards.model.Flashcard;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Data required to create a new flashcard")
public class CreateFlashcardDto {
    @Schema(description = "ID of the deck where this flashcard will be created", example = "507f1f77bcf86cd799439012")
    @NotBlank(message = "Deck ID is required")
    private String deckId;

    @Schema(description = "ID of the user creating this flashcard", example = "507f1f77bcf86cd799439013")
    @NotBlank(message = "User ID is required")
    private String userId;

    @Schema(description = "Front side content (question/prompt) with text and optional code blocks")
    @NotNull(message = "Front content is required")
    @Valid
    private FlashcardDto.CardContentDto front;

    @Schema(description = "Back side content (answer/explanation) with text and optional code blocks")
    @NotNull(message = "Back content is required")
    @Valid
    private FlashcardDto.CardContentDto back;

    @Schema(description = "Optional hint to help with recall (max 200 characters)", example = "Think about variable scope in JavaScript")
    @Size(max = 200, message = "Hint must not exceed 200 characters")
    private String hint;

    @Schema(description = "Tags for categorizing and searching (max 10 tags, 1-30 chars each)", example = "[\"javascript\", \"closures\", \"programming\"]")
    @Size(max = 10, message = "Maximum 10 tags allowed")
    private List<@Size(min = 1, max = 30, message = "Each tag must be between 1 and 30 characters") String> tags;

    @Schema(description = "Difficulty level of the flashcard")
    private Flashcard.DifficultyLevel difficulty;
}